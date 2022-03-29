package com.mercadopago.android.px.internal.services

import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.services.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PreparePaymentService {

    @POST("${BuildConfig.API_ENVIRONMENT}/px_mobile/payments/prepare")
    suspend fun prepare(
        @Body body: PreparePaymentBody
    ): Response<PreparePaymentResponse>
}
