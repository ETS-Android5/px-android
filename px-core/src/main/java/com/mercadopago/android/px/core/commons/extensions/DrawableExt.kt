package com.mercadopago.android.px.core.commons.extensions

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import com.mercadopago.android.px.core.commons.utils.ColorUtils.safeParcelColor

@JvmOverloads
fun Drawable?.setColorFilter(backgroundColor: String?, defaultColor: Int = Color.TRANSPARENT) {
    this?.setColorFilter(
        safeParcelColor(backgroundColor, defaultColor),
        PorterDuff.Mode.SRC_ATOP
    )
}
