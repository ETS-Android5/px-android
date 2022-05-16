package com.mercadopago.android.px.core.presentation.extensions

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.LayoutRes
import androidx.core.graphics.toColorFilter
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.mercadolibre.android.andesui.snackbar.AndesSnackbar
import com.mercadolibre.android.andesui.snackbar.action.AndesSnackbarAction
import com.mercadolibre.android.andesui.snackbar.duration.AndesSnackbarDuration
import com.mercadolibre.android.andesui.snackbar.type.AndesSnackbarType
import com.mercadopago.android.px.core.R
import com.mercadopago.android.px.core.commons.extensions.orIfEmpty

/**
 * Shows the andes snackbar when the view and the context are not null
 *
 * @param message The text to show in the snackbar
 * @param andesSnackbarType Snackbar style provided from Andes.
 * @param andesSnackbarDuration Snackbar duration on screen provided from Andes.
 * @param andesSnackbarAction Nullable snackbar action (callback with text)
 */
fun View?.showSnackBar(
    message: String = "",
    andesSnackbarType: AndesSnackbarType = AndesSnackbarType.ERROR,
    andesSnackbarDuration: AndesSnackbarDuration = AndesSnackbarDuration.LONG,
    andesSnackbarAction: AndesSnackbarAction? = null
) {
    this?.let { view ->
        view.context?.let { context ->
            AndesSnackbar(context,
                view,
                andesSnackbarType,
                message.orIfEmpty(context.getString(R.string.px_error_title)),
                andesSnackbarDuration
            ).also { it.action = andesSnackbarAction }.show()
        }
    }
}

/**
 * Adds a layout change listener when the view is not null.
 * Also if the view isLaidOut it executes the listener.
 */
fun View?.addOnLaidOutListener(onLaidOut: ((view: View) -> Unit)) {
    this?.let {
        if (ViewCompat.isLaidOut(it)) {
            onLaidOut.invoke(it)
        }
        it.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ -> onLaidOut.invoke(view) }
    }
}

/**
 * Wrapper to set a View height
 */
fun View?.setHeight(height: Int) {
    this?.let { view ->
        val layout = view.layoutParams.also {
            it.height = height
        }
        view.layoutParams = layout
    }
}

/**
 * Resets the drawable background color filter to [Color.TRANSPARENT]
 */
fun View.resetDrawableBackgroundColor() {
    background?.colorFilter = PorterDuff.Mode.SRC.toColorFilter(Color.TRANSPARENT)
}

/**
 * Sets the drawable background color filter color when it's a valid color.
 * If it's not a valid color then it resets the background color to [Color.TRANSPARENT]
 *
 * @param color Nullable color in #RRGGBB or #AARRGGBB
 */
fun View.setDrawableBackgroundColor(color: String?) {
    background?.let {
        it.colorFilter = runCatching {
            PorterDuff.Mode.SRC.toColorFilter(color!!.toColorInt())
        }.getOrDefault(
            PorterDuff.Mode.SRC.toColorFilter(Color.TRANSPARENT)
        )
    }
}

/**
 * Wrapper to inflate a layout with attachToRoot false
 */
fun ViewGroup.inflate(@LayoutRes layout: Int): View = LayoutInflater.from(context).inflate(layout, this, false)

/**
 * Applies a gray scale to a view group and it's children recursively
 */
fun ViewGroup.grayScaleViewGroup() {
    loop@ for (i in 0 until childCount) {
        when (val view = getChildAt(i)) {
            is ImageView -> view.grayScaleView()
            is ViewGroup -> view.grayScaleViewGroup()
            else -> continue@loop
        }
    }
}

fun View.visible() {
    isVisible = true
}

fun View.gone() {
    isGone = true
}

fun View.invisible() {
    isInvisible = true
}

inline fun View.loadOrGone(condition: Boolean, block: () -> Unit) {
    if (condition) {
        block()
        visible()
    } else {
        gone()
    }
}
