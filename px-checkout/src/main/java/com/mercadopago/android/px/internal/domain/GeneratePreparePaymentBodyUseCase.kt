package com.mercadopago.android.px.internal.domain

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.ChargeRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.Split
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.DiscountParamsConfigurationDM
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.preferences.CheckoutPreference
import com.mercadopago.android.px.tracking.internal.MPTracker

internal class GeneratePreparePaymentBodyUseCase(
    private val paymentSettingRepository: PaymentSettingRepository,
    private val userSelectionRepository: UserSelectionRepository,
    private val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val discountRepository: DiscountRepository,
    private val chargeRepository: ChargeRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Unit, PreparePaymentBody>(tracker) {

    override suspend fun doExecute(param: Unit): Response<PreparePaymentBody, MercadoPagoError> {
        val preference = checkNotNull(paymentSettingRepository.checkoutPreference) { "Checkout preference must not be null" }
        val discountParamsCfg = with(paymentSettingRepository.advancedConfiguration.discountParamsConfiguration) {
            DiscountParamsConfigurationDM(labels, productId, additionalParams)
        }
        val splitConfiguration = amountConfigurationRepository.getCurrentConfiguration().splitConfiguration
        val paymentMethod = makePaymentMethodDM(preference, splitConfiguration)
        val charges = chargeRepository.customCharges.map {
            PreparePaymentBody.ChargeRuleDM(it.paymentTypeId, it.charge())
        }
        return Response.Success(
            PreparePaymentBody(
                paymentMethod,
                discountParamsCfg,
                preference.marketplace,
                preference.items,
                charges,
                paymentSettingRepository.publicKey
            )
        )
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
