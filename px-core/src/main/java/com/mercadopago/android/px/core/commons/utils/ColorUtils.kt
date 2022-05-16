package com.mercadopago.android.px.core.commons.utils

import android.graphics.Color

object ColorUtils {

    @JvmStatic
    fun safeParcelColor(color: String?, defaultColor: Int) = runCatching {
        Color.parseColor(color)
    }.getOrDefault(defaultColor)
}
