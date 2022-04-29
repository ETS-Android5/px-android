package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.core.AuthorizationProvider
import com.mercadopago.android.px.internal.core.PermissionHelper
import com.mercadopago.android.px.internal.features.payment_result.remedies.AlternativePayerPaymentMethodsMapper
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesBodyMapper
import com.mercadopago.android.px.internal.repository.AmountRepository
import com.mercadopago.android.px.internal.repository.CongratsRepository
import com.mercadopago.android.px.internal.repository.CongratsRepository.PostPaymentCallback
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.OneTapItemRepository
import com.mercadopago.android.px.internal.repository.PayerComplianceRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.internal.services.CongratsService
import com.mercadopago.android.px.internal.tracking.TrackingRepository
import com.mercadopago.android.px.internal.util.StatusHelper
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.BusinessPayment
import com.mercadopago.android.px.model.Currency
import com.mercadopago.android.px.model.IPaymentDescriptor
import com.mercadopago.android.px.model.IPaymentDescriptorHandler
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.PaymentResult
import com.mercadopago.android.px.model.internal.CongratsResponse
import com.mercadopago.android.px.model.internal.remedies.RemediesResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class CongratsRepositoryImpl(
    private val congratsService: CongratsService,
    private val platform: String,
    private val trackingRepository: TrackingRepository,
    private val userSelectionRepository: UserSelectionRepository,
    private val amountRepository: AmountRepository,
    private val disabledPaymentMethodRepository: DisabledPaymentMethodRepository,
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val oneTapItemRepository: OneTapItemRepository,
    private val paymentSettingRepository: PaymentSettingRepository,
    private val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val alternativePayerPaymentMethodsMapper: AlternativePayerPaymentMethodsMapper,
    private val authorizationProvider: AuthorizationProvider) : CongratsRepository {

    private val paymentRewardCache = HashMap<String, CongratsResponse>()
    private val remediesCache = HashMap<String, RemediesResponse>()

    override fun getPostPaymentData(payment: IPaymentDescriptor, paymentResult: PaymentResult,
        callback: PostPaymentCallback) {
        val whiteLabel = TextUtil.isEmpty(authorizationProvider.privateKey)
        val isSuccess = StatusHelper.isSuccess(payment)
        CoroutineScope(Dispatchers.IO).launch {
            val paymentId = payment.paymentIds?.get(0) ?: payment.id.toString()
            val congrats = when {
                whiteLabel || !isSuccess -> CongratsResponse.EMPTY
                paymentRewardCache.containsKey(paymentId) -> paymentRewardCache[paymentId]!!
                else -> getCongratsResponse(payment, paymentResult).apply { paymentRewardCache[paymentId] = this }
            }
            val remedies = when {
                whiteLabel || isSuccess || payment is BusinessPayment -> RemediesResponse.EMPTY
                remediesCache.containsKey(paymentId) -> remediesCache[paymentId]!!
                else -> {
                    getRemedies(payment, paymentResult.paymentData).apply { remediesCache[paymentId] = this }
                }
            }
            withContext(Dispatchers.Main) {
                handleResult(payment, paymentResult, congrats, remedies, paymentSettingRepository.currency, callback)
            }
        }
    }

    private suspend fun getCongratsResponse(payment: IPaymentDescriptor, paymentResult: PaymentResult) =
        try {
            val joinedPaymentIds = TextUtil.join(payment.paymentIds)
            val joinedPaymentMethodsIds = paymentResult.paymentDataList
                .joinToString(TextUtil.CSV_DELIMITER) { p -> (p.paymentMethod.id) }
            val campaignId = paymentResult.paymentData.campaign?.id.orEmpty()
            val paymentTypeId = paymentResult.paymentData.paymentMethod.paymentTypeId
            with(paymentSettingRepository) {
                congratsService.getCongrats(
                    PermissionHelper.instance.isLocationGranted(),
                    publicKey,
                    joinedPaymentIds,
                    platform,
                    campaignId,
                    joinedPaymentMethodsIds,
                    paymentTypeId,
                    trackingRepository.flowId,
                    checkoutPreference?.merchantOrderId,
                    checkoutPreference?.id
                )
            }
        } catch (e: Exception) {
            CongratsResponse.EMPTY
        }

    private suspend fun getRemedies(payment: IPaymentDescriptor, paymentData: PaymentData) =
        try {
            val hasOneTap = oneTapItemRepository.value.isNotEmpty()
            val usedPayerPaymentMethodId = paymentData.token?.cardId ?: paymentData.paymentMethod.id
            val escCardIds = escManagerBehaviour.escCardIds
            val body = RemediesBodyMapper(
                userSelectionRepository,
                amountRepository,
                usedPayerPaymentMethodId,
                escCardIds.contains(usedPayerPaymentMethodId),
                alternativePayerPaymentMethodsMapper.map(payerPaymentMethodRepository.value).filter {
                    it.customOptionId != usedPayerPaymentMethodId &&
                        !disabledPaymentMethodRepository.hasKey(
                            PayerPaymentMethodKey(it.customOptionId, it.paymentTypeId))
                },
                paymentSettingRepository,
                payerPaymentMethodRepository
            ).map(paymentData)
            congratsService.getRemedies(
                payment.id.toString(),
                hasOneTap,
                body
            )
        } catch (e: Exception) {
            RemediesResponse.EMPTY
        }

    private fun handleResult(payment: IPaymentDescriptor, paymentResult: PaymentResult, congrats: CongratsResponse,
        remedies: RemediesResponse, currency: Currency, callback: PostPaymentCallback) {
        payment.process(object : IPaymentDescriptorHandler {
            override fun visit(payment: IPaymentDescriptor) {
                callback.handleResult(PaymentModel(payment, paymentResult, congrats, remedies, currency))
            }

            override fun visit(businessPayment: BusinessPayment) {
                callback.handleResult(BusinessPaymentModel(businessPayment, paymentResult, congrats, remedies,
                    currency))
            }
        })
    }
}
