package com.mercadopago.android.px.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenericCardDisplayInfo(
    val iconUrl: String?,
    val border: PXBorder?,
    val backgroundColor: String?,
    val shadow: Boolean?
) : Parcelable
