package com.mercadopago.android.px.model.internal

data class PaymentMethodBehaviourDM(
    val paymentTypeRules: List<String> = emptyList(),
    val paymentMethodRules: List<String> = emptyList(),
    val sliderTitle: String,
    val behaviours: List<BehaviourDM> = emptyList()
)
