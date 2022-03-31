package com.mercadopago.android.px.model.internal.payment_prepare

class PaymentMethodDM(
    val paymentMethodType: String,
    val paymentMethodId: String,
    val cardInfo: CardInfo?,
    val source: Collection<String>,
    val splitPaymentMethods: Collection<PaymentMethodDM>,
    val discountInfo: DiscountInfo?
) {
    data class DiscountInfo(
        val campaignId: String? = null,
        val discountToken: String? = null
    )

    data class CardInfo(
        val issuerId: Long,
        val bin: String,
        val cardId: String
    )
}
