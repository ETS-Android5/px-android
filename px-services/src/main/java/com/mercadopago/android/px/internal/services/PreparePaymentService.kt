package com.mercadopago.android.px.internal.services

import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.services.BuildConfig
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PreparePaymentService {

    //@POST("${BuildConfig.API_ENVIRONMENT}/px_mobile/payments/prepare")
    //@POST("https://run.mocky.io/v3/1912478c-a191-4e6f-8801-88b9cdfe0b0d")
    @POST("https://run.mocky.io/v3/fcc5cbd8-6524-4e77-859e-e0756d40ff06")
    suspend fun prepare(
        @Body body: PreparePaymentBody
    ): Response<PreparePaymentResponse>
}
