package com.mercadopago.android.px.model.internal

data class TextDM(
    val message: String,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val weight: String? = null,
    val alignment: TextAlignment? = null
)
