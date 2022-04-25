package com.mercadopago.android.px.model

data class Tokenization(
    val lastModifiedDate: String,
    val tokenizationId: String,
    val status: String,
    val bindingTokenizationId: String,
    val boundDevices: List<String>
)
