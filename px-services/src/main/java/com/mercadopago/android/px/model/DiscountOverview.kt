package com.mercadopago.android.px.model

import android.os.Parcelable
import com.mercadopago.android.px.model.internal.Text
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DiscountOverview(
    val description: List<Text>,
    val amount: Text,
    val brief: List<Text>?,
    val url: String?
) : Parcelable
