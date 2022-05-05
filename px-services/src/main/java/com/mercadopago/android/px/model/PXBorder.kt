package com.mercadopago.android.px.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PXBorder(val type: PXBorderType, val color: String) : Parcelable {

    enum class PXBorderType {
        @SerializedName("solid") SOLID,
        @SerializedName("dotted") DOTTED
    }
}
