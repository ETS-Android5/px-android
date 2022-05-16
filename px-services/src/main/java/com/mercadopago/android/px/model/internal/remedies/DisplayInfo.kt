package com.mercadopago.android.px.model.internal.remedies

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DisplayInfo(val header: Header) : Parcelable {
    @Parcelize
    data class Header(val title: String, val iconUrl: String? = null, val badgeUrl: String? = null) : Parcelable
}
