package com.mercadopago.android.px.core.presentation.extensions

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import com.mercadopago.android.px.core.presentation.utils.ColorUtils.safeParseColor

@JvmOverloads
fun Drawable?.setColorFilter(backgroundColor: String?, defaultColor: Int = Color.TRANSPARENT) {
    this?.setColorFilter(
        safeParseColor(backgroundColor, defaultColor),
        PorterDuff.Mode.SRC_ATOP
    )
}
