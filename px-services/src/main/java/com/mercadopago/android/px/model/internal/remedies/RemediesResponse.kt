package com.mercadopago.android.px.model.internal.remedies

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RemediesResponse(val cvv: CvvRemedyResponse?,
    val highRisk: HighRiskRemedyResponse?,
    val suggestedPaymentMethod: SuggestedPaymentMethod?,
    val displayInfo: DisplayInfo?,
    val trackingData: Map<String, String>?
) : Parcelable {

    fun hasRemedies() = suggestedPaymentMethod != null || highRisk != null || cvv != null

    companion object {
        @JvmStatic val EMPTY = RemediesResponse(null, null, null, null, null)
    }

    @Parcelize
    data class Action(val label: String): Parcelable
}
