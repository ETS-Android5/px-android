package com.mercadopago.android.px.core.presentation.utils

import android.graphics.Color

object ColorUtils {

    @JvmStatic
    fun safeParseColor(color: String?, defaultColor: Int) = runCatching {
        Color.parseColor(color)
    }.getOrDefault(defaultColor)
}
