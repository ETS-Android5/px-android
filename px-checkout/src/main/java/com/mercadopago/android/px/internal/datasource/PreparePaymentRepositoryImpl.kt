package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.internal.adapters.NetworkApi
import com.mercadopago.android.px.internal.callbacks.ApiResponse
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.internal.services.PreparePaymentService
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.DiscountParamsConfigurationDM
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse

internal class PreparePaymentRepositoryImpl(
    private val paymentSettingRepository: PaymentSettingRepository,
    private val userSelectionRepository: UserSelectionRepository,
    private val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val preparePaymentService: PreparePaymentService,
    private val networkApi: NetworkApi
) : PreparePaymentRepository {

    override suspend fun prepare(): Response<PreparePaymentResponse, MercadoPagoError> {
        val discountParamsCfg = with(paymentSettingRepository.advancedConfiguration.discountParamsConfiguration) {
            DiscountParamsConfigurationDM(
                labels,
                productId,
                additionalParams
            )
        }
        val paymentMethod = with(userSelectionRepository) {
            checkNotNull(paymentMethod) { "User selected payment method must not be null" }
            checkNotNull(customOptionId) { "User selected custom option id must not be null" }
            mapPaymentMethodDM(paymentMethod!!, secondaryPaymentMethod, card, customOptionId!!)
        }
        val body = PreparePaymentBody(
            paymentMethod,
            discountParamsCfg,
            paymentSettingRepository.checkoutPreferenceId,
            paymentSettingRepository.checkoutPreference,
            paymentSettingRepository.publicKey
        )
        val apiResponse = networkApi.apiCallForResponse(PreparePaymentService::class.java) {
            preparePaymentService.prepare(body)
        }
        return when (apiResponse) {
            is ApiResponse.Failure -> Response.Failure(
                MercadoPagoError(
                    apiResponse.exception,
                    ApiUtil.RequestOrigin.PREPARE_PAYMENT
                )
            )
            is ApiResponse.Success -> Response.Success(apiResponse.result)
        }
    }

    private fun mapPaymentMethodDM(
        primaryPaymentMethod: PaymentMethod,
        secondaryPaymentMethod: PaymentMethod? = null,
        card: Card? = null,
        customOptionId: String
    ): PaymentMethodDM {
        return PaymentMethodDM(
            primaryPaymentMethod.paymentTypeId,
            primaryPaymentMethod.id,
            card?.issuer?.id,
            card?.id,
            card?.firstSixDigits,
            listOfNotNull(
                secondaryPaymentMethod?.let {
                    mapPaymentMethodDM(secondaryPaymentMethod, null, null, it.id)
                }
            ),
            PaymentMethodDM.DiscountInfo(
                payerPaymentMethodRepository[customOptionId]?.defaultAmountConfiguration
            )
        )
    }
}
