package com.mercadopago.android.px.model.internal.payment_prepare

import com.mercadopago.android.px.model.Item
import com.mercadopago.android.px.model.internal.DiscountParamsConfigurationDM
import com.mercadopago.android.px.preferences.CheckoutPreference
import java.math.BigDecimal

data class PreparePaymentBody(
    val paymentMethod: PaymentMethodDM,
    val discountConfiguration: DiscountParamsConfigurationDM,
    val marketplace: String,
    val items: List<Item>,
    val charges: List<ChargeRuleDM>,
    val publicKey: String
) {
    data class ChargeRuleDM(
        val paymentTypeId: String,
        val charge: BigDecimal
    )
}
