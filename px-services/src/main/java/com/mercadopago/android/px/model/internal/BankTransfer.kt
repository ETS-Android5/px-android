package com.mercadopago.android.px.model.internal

data class BankTransfer(val id: String, val displayInfo: DisplayInfo) {
    data class DisplayInfo(
        val color: String,
        val paymentMethodImageUrl: String,
        val title: Text,
        val subtitle: Text?,
        val description: Text?,
        val gradientColor: List<String>?,
        val sliderTitle: String?
    )
}
