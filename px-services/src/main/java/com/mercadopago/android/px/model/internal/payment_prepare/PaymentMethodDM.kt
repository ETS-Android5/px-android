package com.mercadopago.android.px.model.internal.payment_prepare

class PaymentMethodDM(
    val paymentMethodType: String,
    val paymentMethodId: String,
    val issuerId: Long?,
    val bin: String?,
    val cardId: String?,
    val source: Collection<String>,
    val with: Collection<PaymentMethodDM>,
    val discountInfo: DiscountInfo?
) {
    data class DiscountInfo(
        val campaignId: String?,
        val discountToken: String?
    )
}
