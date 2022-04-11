package com.mercadopago.android.px.addons.tracking

enum class Tracker(val bit: Long) {
    MELIDATA(1 shl 0),
    GOOGLE_ANALYTICS(1 shl 1),
    CUSTOM(1 shl 2),
    GOOGLE_ANALYTICS_V2(1 shl 3);

    fun shouldTrack(mask: Long) = mask and bit == bit
}