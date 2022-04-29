package com.mercadopago.android.px.model.internal

data class OfflineMethodCard(val displayInfo: DisplayInfo) {
    data class DisplayInfo(
        val color: String,
        val paymentMethodImageUrl: String,
        val title: Text,
        val subtitle: Text?,
        val gradientColor: List<String>?
    )
}
