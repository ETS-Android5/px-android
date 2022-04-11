package com.mercadopago.android.px.model.internal

import com.google.gson.annotations.SerializedName

/**
 * Represents the status of a particular section (i.e. discount section) of the response.
 */
data class ResponseSectionStatus(
    val code: Code,
    val message: String?
) {
    enum class Code {
        @SerializedName("PX_OK") OK,
        @SerializedName("PX_MISMATCH") MISMATCH,
        @SerializedName("PX_USE_FALLBACK") USE_FALLBACK,
    }
}
