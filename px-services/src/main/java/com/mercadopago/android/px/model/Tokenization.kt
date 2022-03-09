package com.mercadopago.android.px.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
class Tokenization(
    val last_modified_date: String,
    val tokenization_id: String,
    val status: String,
    val binding_tokenization_id: String,
    val bound_devices: String?
) : Parcelable, Serializable