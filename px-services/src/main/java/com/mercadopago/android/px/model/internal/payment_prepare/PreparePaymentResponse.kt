package com.mercadopago.android.px.model.internal.payment_prepare

import com.mercadopago.android.px.model.internal.ResponseSectionStatus

data class PreparePaymentResponse(
    val status: Map<String, ResponseSectionStatus>?,
    val paymentMethod: PaymentMethodDM?
) {
    companion object {
        const val SECTION_DISCOUNTS = "discounts"
    }
}

fun PreparePaymentResponse.getDiscountStatus() = status?.get(PreparePaymentResponse.SECTION_DISCOUNTS)
