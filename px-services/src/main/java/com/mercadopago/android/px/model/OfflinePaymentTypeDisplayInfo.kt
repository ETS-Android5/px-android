package com.mercadopago.android.px.model

import android.os.Parcelable
import com.mercadopago.android.px.model.internal.Text
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OfflinePaymentTypeDisplayInfo(
    val bottomDescription: Text?
) : Parcelable
