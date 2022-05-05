package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.internal.adapters.NetworkApi
import com.mercadopago.android.px.internal.callbacks.ApiResponse
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.internal.services.PreparePaymentService
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentBody
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse

internal class PreparePaymentRepositoryImpl(
    private val networkApi: NetworkApi
) : PreparePaymentRepository {

    override suspend fun prepare(body: PreparePaymentBody): Response<PreparePaymentResponse, MercadoPagoError> {
        val apiResponse = networkApi.apiCallForResponse(PreparePaymentService::class.java) {
            it.prepare(body)
        }
        return when (apiResponse) {
            is ApiResponse.Failure -> Response.Failure(
                MercadoPagoError(apiResponse.exception, ApiUtil.RequestOrigin.PREPARE_PAYMENT)
            )
            is ApiResponse.Success -> {
                Response.Success(apiResponse.result)
            }
        }
    }
}
