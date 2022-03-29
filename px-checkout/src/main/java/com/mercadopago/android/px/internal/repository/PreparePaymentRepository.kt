package com.mercadopago.android.px.internal.repository

import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse

internal interface PreparePaymentRepository {
    suspend fun prepare() : Response<PreparePaymentResponse, MercadoPagoError>
}
