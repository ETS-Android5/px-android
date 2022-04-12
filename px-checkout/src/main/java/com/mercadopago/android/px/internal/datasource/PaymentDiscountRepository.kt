package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.repository.PaymentDiscountRepositoryImpl
import com.mercadopago.android.px.internal.repository.PaymentDiscountRepositoryImpl.*
import com.mercadopago.android.px.model.Discount

/**
 * Used to store the discount data we will send to the payment processor.
 */
internal interface PaymentDiscountRepository {
    val discounts: PaymentDiscounts
    fun configure(discounts: PaymentDiscounts)
    fun reset()

    data class PaymentDiscounts(
        val primaryPaymentMethodDiscount: Discount?,
        val splitPaymentMethodDiscount: Discount?
    )
}
