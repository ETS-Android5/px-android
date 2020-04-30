package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.features.payment_result.remedies.AlternativePayerPaymentMethodsMapper
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesBodyMapper
import com.mercadopago.android.px.internal.repository.*
import com.mercadopago.android.px.internal.repository.CongratsRepository.PostPaymentCallback
import com.mercadopago.android.px.internal.services.CongratsService
import com.mercadopago.android.px.internal.util.StatusHelper
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.internal.viewmodel.BusinessPaymentModel
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.internal.CongratsResponse
import com.mercadopago.android.px.model.internal.InitResponse
import com.mercadopago.android.px.model.internal.remedies.RemediesResponse
import com.mercadopago.android.px.services.BuildConfig
import com.mercadopago.android.px.internal.services.Response
import com.mercadopago.android.px.internal.services.awaitCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CongratsRepositoryImpl(
    private val congratsService: CongratsService, private val initService: InitRepository,
    private val paymentSetting: PaymentSettingRepository, private val platform: String, private val locale: String,
    private val flow: String?, private val userSelectionRepository: UserSelectionRepository,
    private val amountRepository: AmountRepository,
    private val disabledPaymentMethodRepository : DisabledPaymentMethodRepository,
    private val payerComplianceRepository: PayerComplianceRepository,
    private val escManagerBehaviour: ESCManagerBehaviour) : CongratsRepository {

    private val paymentRewardCache = HashMap<String, CongratsResponse>()
    private val remediesCache = HashMap<String, RemediesResponse>()
    private val privateKey = paymentSetting.privateKey

    override fun getPostPaymentData(payment: IPaymentDescriptor, paymentResult: PaymentResult,
        callback: PostPaymentCallback) {
        val hasAccessToken = TextUtil.isNotEmpty(privateKey)
        val hasToReturnEmptyResponse = !hasAccessToken
        val isSuccess = StatusHelper.isSuccess(payment)
        CoroutineScope(Dispatchers.IO).launch {
            val paymentId = payment.paymentIds?.get(0) ?: payment.id.toString()
            val paymentReward = when {
                hasToReturnEmptyResponse || !isSuccess -> CongratsResponse.EMPTY
                paymentRewardCache.containsKey(paymentId) -> paymentRewardCache[paymentId]!!
                else -> getPaymentReward(payment, paymentResult).apply { paymentRewardCache[paymentId] = this }
            }
            val remediesResponse = when {
                hasToReturnEmptyResponse || isSuccess -> RemediesResponse.EMPTY
                remediesCache.containsKey(paymentId) -> remediesCache[paymentId]!!
                else -> {
                    getRemedies(payment, paymentResult.paymentData)
                }
            }
            withContext(Dispatchers.Main) {
                handleResult(payment, paymentResult, paymentReward, remediesResponse, paymentSetting.currency, callback)
            }
        }
    }

    private suspend fun getPaymentReward(payment: IPaymentDescriptor, paymentResult: PaymentResult) =
        try {
            val joinedPaymentIds = TextUtil.join(payment.paymentIds)
            val joinedPaymentMethodsIds = paymentResult.paymentDataList
                .joinToString(TextUtil.CSV_DELIMITER) { p -> (p.paymentMethod.id) }
            val campaignId = paymentResult.paymentData.campaign?.run { id } ?: ""
            val response = congratsService.getCongrats(BuildConfig.API_ENVIRONMENT, locale, privateKey,
                joinedPaymentIds, platform, campaignId, payerComplianceRepository.turnedIFPECompliant(),
                joinedPaymentMethodsIds, flow).await()
            if (response.isSuccessful) {
                response.body()!!
            } else {
                CongratsResponse.EMPTY
            }
        } catch (e: Exception) {
            CongratsResponse.EMPTY
        }

    private fun getPayerPaymentMethods(response: InitResponse?) =
        mutableListOf<Triple<SecurityCode?, String, CustomSearchItem>>()
            .also { mapPayerPaymentMethods ->
                response?.run {
                    customSearchItems.forEach { customSearchItem ->
                        express.find { it.customOptionId == customSearchItem.id }?.let { expressMetadata ->
                            mapPayerPaymentMethods.add(
                                Triple(getCardById(customSearchItem.id)?.securityCode, expressMetadata.customOptionId,
                                    customSearchItem)
                            )
                        }
                    }
                }
            }.filter { !disabledPaymentMethodRepository.hasPaymentMethodId(it.second) }

    private suspend fun loadInitResponse() =
        when (val callbackResult = initService.init().awaitCallback<InitResponse>()) {
            is Response.Success<*> -> callbackResult.result as InitResponse
            is Response.Failure<*> -> null
        }

    private suspend fun getRemedies(payment: IPaymentDescriptor, paymentData: PaymentData) =
        try {
            val initResponse = loadInitResponse()
            val payerPaymentMethods = getPayerPaymentMethods(initResponse)
            val hasOneTap = initResponse?.hasExpressCheckoutMetadata() ?: false
            val customOptionId = paymentData.token?.cardId ?: paymentData.paymentMethod.id
            val escCardIds = escManagerBehaviour.escCardIds
            val body = RemediesBodyMapper(
                userSelectionRepository,
                amountRepository,
                customOptionId,
                escCardIds.contains(customOptionId),
                AlternativePayerPaymentMethodsMapper(escCardIds).map(payerPaymentMethods.filter { it.second != customOptionId })
            ).map(paymentData)
            val response = congratsService.getRemedies(
                BuildConfig.API_ENVIRONMENT_NEW,
                payment.id.toString(),
                locale,
                privateKey,
                hasOneTap,
                body
            ).await()

            if (response.isSuccessful) {
                response.body()!!
            } else {
                RemediesResponse.EMPTY
            }
        } catch (e: Exception) {
            RemediesResponse.EMPTY
        }

    private fun handleResult(payment: IPaymentDescriptor, paymentResult: PaymentResult, congratsResponse: CongratsResponse,
        remedies: RemediesResponse, currency: Currency, callback: PostPaymentCallback) {
        payment.process(object : IPaymentDescriptorHandler {
            override fun visit(payment: IPaymentDescriptor) {
                callback.handleResult(PaymentModel(payment, paymentResult, congratsResponse, remedies, currency))
            }

            override fun visit(businessPayment: BusinessPayment) {
                callback.handleResult(BusinessPaymentModel(businessPayment, paymentResult, congratsResponse, remedies,
                    currency))
            }
        })
    }
}