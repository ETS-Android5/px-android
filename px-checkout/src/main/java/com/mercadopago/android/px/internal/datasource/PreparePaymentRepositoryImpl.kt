package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.internal.adapters.NetworkApi
import com.mercadopago.android.px.internal.callbacks.ApiResponse
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.extensions.isNotNull
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
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
            mapPaymentMethodDM(
                paymentMethod!!,
                // In future we might have more than one secondary payment method so we pass a list
                if (secondaryPaymentMethod.isNotNull()) listOf(secondaryPaymentMethod!!) else null,
                card,
                customOptionId!!
            )
        }
        val body = with(paymentSettingRepository) {
            PreparePaymentBody(
                paymentMethod,
                discountParamsCfg,
                checkoutPreferenceId,
                checkoutPreference,
                publicKey
            )
        }
        val apiResponse = networkApi.apiCallForResponse(PreparePaymentService::class.java) {
            it.prepare(body)
        }
        return when (apiResponse) {
            is ApiResponse.Failure -> Response.Failure(
                MercadoPagoError(
                    apiResponse.exception,
                    ApiUtil.RequestOrigin.PREPARE_PAYMENT
                )
            )
            is ApiResponse.Success -> {
                Response.Success(apiResponse.result)
            }
        }
    }

    private fun mapPaymentMethodDM(
        primaryPaymentMethod: PaymentMethod,
        splitPaymentMethods: List<PaymentMethod>? = null,
        card: Card? = null,
        customOptionId: String
    ): PaymentMethodDM {
        return PaymentMethodDM(
            primaryPaymentMethod.id,
            primaryPaymentMethod.paymentTypeId,
            card?.let { PaymentMethodDM.CardInfo(it.id!!, it.issuer?.id!!, it.firstSixDigits!!) },
            emptyList(), // TODO: Replace with source list for meli coins when it's added
            splitPaymentMethods?.map { mapPaymentMethodDM(it, null, null, it.id) },
            PaymentMethodDM.DiscountInfo(payerPaymentMethodRepository[customOptionId]?.defaultAmountConfiguration)
        )
    }
}
