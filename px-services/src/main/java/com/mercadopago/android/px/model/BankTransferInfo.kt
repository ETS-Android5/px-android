package com.mercadopago.android.px.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class BankTransferInfo(val name: String) : Parcelable
