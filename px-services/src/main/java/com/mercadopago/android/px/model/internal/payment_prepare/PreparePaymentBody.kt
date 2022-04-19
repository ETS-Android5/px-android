package com.mercadopago.android.px.model.internal.payment_prepare

import com.mercadopago.android.px.model.internal.DiscountParamsConfigurationDM
import com.mercadopago.android.px.preferences.CheckoutPreference

data class PreparePaymentBody(
    val paymentMethod: PaymentMethodDM,
    val discountConfiguration: DiscountParamsConfigurationDM,
    val marketplace: String,
    val publicKey: String
)
