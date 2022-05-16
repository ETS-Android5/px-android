package com.mercadopago.android.px.model.display_info

import android.os.Parcelable
import com.mercadopago.android.px.model.internal.Text
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class CustomSearchItemDisplayInfo(val result: Result) : Serializable, Parcelable {
    @Parcelize
    data class Result(val paymentMethod: PaymentMethod?, val extraInfo: ExtraInfo?) : Serializable, Parcelable {
        @Parcelize
        data class PaymentMethod(val iconUrl: String, val detail: List<Text>) : Serializable, Parcelable

        @Parcelize
        data class ExtraInfo(val detail: List<Text>) : Serializable, Parcelable
    }
}
