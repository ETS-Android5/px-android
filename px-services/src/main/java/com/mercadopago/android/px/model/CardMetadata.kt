package com.mercadopago.android.px.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class CardMetadata(
    val id: String,
    val displayInfo: CardDisplayInfo,
    val retry: Retry,
    val tokenization: Tokenization? = null
) : Parcelable, Serializable