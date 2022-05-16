package com.mercadopago.android.px.addons.model

import java.math.BigDecimal

data class PaymentMethodReauthModel(
    val amount: BigDecimal,
    val id: String?,
    val type: String?
)
