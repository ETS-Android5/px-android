package com.mercadopago.android.px.model.internal.payment_prepare

class PaymentMethodDM(
    val id: String,
    val paymentTypeId: String,
    val cardInfo: CardInfo?,
    val source: List<String>,
    val splitPaymentMethods: List<PaymentMethodDM>?,
    val discountInfo: DiscountInfo?
) {
    data class DiscountInfo(
        val campaignId: String? = null,
        val token: String? = null
    )

    data class CardInfo(
        val id: String,
        val issuerId: Long,
        val bin: String
    )
}
