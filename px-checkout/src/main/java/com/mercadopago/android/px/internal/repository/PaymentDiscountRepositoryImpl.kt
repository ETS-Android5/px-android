package com.mercadopago.android.px.internal.repository

import android.content.SharedPreferences
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository.PaymentDiscounts
import com.mercadopago.android.px.internal.util.JsonUtil

private const val PAYMENT_DISCOUNTS = "PAYMENT_DISCOUNTS"

internal class PaymentDiscountRepositoryImpl(private val sharedPreferences: SharedPreferences) : PaymentDiscountRepository {
    private var internalDiscounts: PaymentDiscounts? = null
    override val discounts: PaymentDiscounts
        get() {
            if (internalDiscounts == null) {
                internalDiscounts =
                    JsonUtil.fromJson(sharedPreferences.getString(PAYMENT_DISCOUNTS, null), PaymentDiscounts::class.java)
            }
            return internalDiscounts ?: PaymentDiscounts(null, null)
        }

    override fun configure(discounts: PaymentDiscounts) {
        this.internalDiscounts = discounts
        sharedPreferences.edit().apply {
            putString(PAYMENT_DISCOUNTS, JsonUtil.toJson(discounts))
            apply()
        }
    }

    override fun reset() {
        sharedPreferences.edit().remove(PAYMENT_DISCOUNTS).apply()
        internalDiscounts = null
    }
}
