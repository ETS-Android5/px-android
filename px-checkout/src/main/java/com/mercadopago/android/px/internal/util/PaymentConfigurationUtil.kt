package com.mercadopago.android.px.internal.util

import com.mercadopago.android.px.configuration.PaymentConfiguration
import com.mercadopago.android.px.core.v2.PaymentProcessor
import com.mercadopago.android.px.preferences.CheckoutPreference

/**
 * Class used to avoid JvmName annotation on payemntProcessorV2
 */
internal object PaymentConfigurationUtil {
    @JvmStatic
    fun getPaymentProcessor(paymentConfiguration: PaymentConfiguration): PaymentProcessor? {
        return paymentConfiguration.paymentProcessorV2
    }

    @JvmStatic
    fun getSupportsSplit(
        paymentConfiguration: PaymentConfiguration,
        checkoutPreference: CheckoutPreference?
    ): Boolean {
        return paymentConfiguration.getSupportsSplit(checkoutPreference)
    }
}