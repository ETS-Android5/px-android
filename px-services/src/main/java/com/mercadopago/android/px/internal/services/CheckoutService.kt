package com.mercadopago.android.px.internal.services

import com.mercadopago.android.px.internal.callbacks.MPCall
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.PaymentMethodSearch
import com.mercadopago.android.px.model.internal.CheckoutResponse
import com.mercadopago.android.px.model.internal.InitRequestBody
import com.mercadopago.android.px.services.BuildConfig
import retrofit2.Response
import retrofit2.http.*
import java.math.BigDecimal

interface CheckoutService {

    @POST("delta/px_mobile/$CHECKOUT_VERSION/checkout")
    suspend fun checkout(
        @Body body: InitRequestBody
    ): Response<CheckoutResponse>

    @POST("delta/px_mobile/$CHECKOUT_VERSION/checkout/{preference_id}")
    suspend fun checkout(
        @Path(value = "preference_id", encoded = true) preferenceId: String,
        @Body body: InitRequestBody
    ): Response<CheckoutResponse>

    /**
     * Old api call version ; used by MercadoPagoServices.
     *
     * @param publicKey
     * @param amount
     * @param excludedPaymentTypes
     * @param excludedPaymentMethods
     * @param siteId
     * @param processingMode
     * @param cardsWithEsc
     * @param differentialPricingId
     * @param defaultInstallments
     * @param expressEnabled
     * @param accessToken
     * @return payment method search
     */
    @GET("{environment}/px_mobile_api/payment_methods?api_version=1.8")
    fun getPaymentMethodSearch(
        @Path(value = "environment", encoded = true) environment: String,
        @Query("public_key") publicKey: String,
        @Query("amount") amount: BigDecimal,
        @Query("excluded_payment_types") excludedPaymentTypes: String,
        @Query("excluded_payment_methods") excludedPaymentMethods: String,
        @Query("site_id") siteId: String,
        @Query("processing_mode") processingMode: String,
        @Query("cards_esc") cardsWithEsc: String,
        @Query("differential_pricing_id") differentialPricingId: Int?,
        @Query("default_installments") defaultInstallments: Int?,
        @Query("express_enabled") expressEnabled: Boolean,
        @Query("access_token") accessToken: String?
    ): MPCall<PaymentMethodSearch>

    @GET("{environment}/payment_methods")
    fun getPaymentMethods(
        @Path(value = "environment", encoded = true) environment: String,
        @Query("public_key") publicKey: String,
        @Query("access_token") privateKey: String
    ): MPCall<List<PaymentMethod>>

    companion object {
        const val CHECKOUT_VERSION = "v2"
        const val ENVIRONMENT = BuildConfig.API_ENVIRONMENT_NEW
    }
}