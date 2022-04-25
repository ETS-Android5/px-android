package com.mercadopago.android.px.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Tokenization(
    val lastModifiedDate: String,
    val tokenizationId: String,
    val status: String,
    val bindingTokenizationId: String,
    val boundDevices: List<String>
) : Parcelable