package com.mercadopago.android.px.model.internal

import android.os.Parcelable
import java.io.Serializable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Text @JvmOverloads constructor(
    val message: String,
    val backgroundColor: String? = null,
    val textColor: String? = null,
    val weight: String? = null,
    val alignment: TextAlignment? = null
) : Parcelable, Serializable
