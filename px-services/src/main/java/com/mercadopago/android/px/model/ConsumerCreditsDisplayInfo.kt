package com.mercadopago.android.px.model

import android.os.Parcelable
import com.mercadopago.android.px.model.display_info.LinkableText
import kotlinx.android.parcel.Parcelize

@Parcelize
 data class ConsumerCreditsDisplayInfo (
    @JvmField
    val color: String?,
    val fontColor: String?,
    @JvmField
    val topText: LinkableText?,
    @JvmField
    val bottomText: LinkableText?,
    @JvmField
    val tag: Tag?
): Parcelable

@Parcelize
data class Tag(val text: String, val backgroundColor: Int, val textColor: Int, val weight: String): Parcelable