package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.internal.adapters.NetworkApi
import com.mercadopago.android.px.internal.callbacks.ApiResponse
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.ChargeRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.internal.services.PreparePaymentService
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.model.Split
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.DiscountParamsConfigurationDM
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.preferences.CheckoutPreference

internal class PreparePaymentRepositoryImpl(
    private val paymentSettingRepository: PaymentSettingRepository,
    private val userSelectionRepository: UserSelectionRepository,
    private val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val discountRepository: DiscountRepository,
    private val chargeRepository: ChargeRepository,
    private val networkApi: NetworkApi
) : PreparePaymentRepository {

    override suspend fun prepare(): Response<PreparePaymentResponse, MercadoPagoError> {
        val preference = checkNotNull(paymentSettingRepository.checkoutPreference) { "Checkout preference must not be null" }
        val discountParamsCfg = with(paymentSettingRepository.advancedConfiguration.discountParamsConfiguration) {
            DiscountParamsConfigurationDM(labels, productId, additionalParams)
        }
        val splitConfiguration = amountConfigurationRepository.getCurrentConfiguration().splitConfiguration
        val paymentMethod = makePaymentMethodDM(preference, splitConfiguration)
        val charges = chargeRepository.customCharges.map {
            PreparePaymentBody.ChargeRuleDM(it.paymentTypeId, it.charge())
        }
        val body = with(paymentSettingRepository) {
            PreparePaymentBody(paymentMethod, discountParamsCfg, preference.marketplace, preference.items, charges, publicKey)
        }
        val apiResponse = networkApi.apiCallForResponse(PreparePaymentService::class.java) {
            it.prepare(body)
        }
        return when (apiResponse) {
            is ApiResponse.Failure -> Response.Failure(
                MercadoPagoError(apiResponse.exception, ApiUtil.RequestOrigin.PREPARE_PAYMENT)
            )
            is ApiResponse.Success -> {
                Response.Success(apiResponse.result)
            }
        }
    }

    private fun makePaymentMethodDM(preference: CheckoutPreference, splitConfiguration: Split?) = with(userSelectionRepository) {
        val primaryPaymentMethod = checkNotNull(paymentMethod) { "User selected payment method must not be null" }
        val customOptionId = checkNotNull(customOptionId) { "User selected custom option id must not be null" }
        val isSplit = secondaryPaymentMethod != null
        val campaignId = payerPaymentMethodRepository[customOptionId]?.defaultAmountConfiguration
        PaymentMethodDM(
            primaryPaymentMethod.id,
            primaryPaymentMethod.paymentTypeId,
            preference.totalAmount,
            card?.let { PaymentMethodDM.CardInfo(it.id!!, it.issuer?.id!!, it.firstSixDigits!!) },
            emptyList(), // TODO: Replace with source list for meli coins when it's added
            listOfNotNull(secondaryPaymentMethod?.let {
                PaymentMethodDM(
                    it.id,
                    it.paymentTypeId,
                    splitConfiguration?.secondaryPaymentMethod?.amount,
                    null,
                    emptyList(),
                    null,
                    splitConfiguration?.secondaryPaymentMethod?.discount?.let { discount ->
                        PaymentMethodDM.DiscountInfo(
                            campaignId = campaignId,
                            couponAmount = discount.couponAmount
                        )
                    }
                )
            }),
            getPrimaryPaymentMethodDiscount(isSplit, splitConfiguration)?.let {
                PaymentMethodDM.DiscountInfo(
                    campaignId = campaignId,
                    couponAmount = it.couponAmount
                )
            }
        )
    }

    private fun getPrimaryPaymentMethodDiscount(isSplit: Boolean, splitConfiguration: Split?) =
        if (isSplit) {
            splitConfiguration!!.primaryPaymentMethod.discount
        } else {
            discountRepository.getCurrentConfiguration().discount
        }
}
