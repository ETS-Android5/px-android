package com.mercadopago.android.px.internal.domain

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.extensions.ifSuccess
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.tracking.internal.MPTracker

internal class PreparePaymentUseCase(
    private val preparePaymentRepository: PreparePaymentRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Unit, PreparePaymentResponse>(tracker) {

    override suspend fun doExecute(param: Unit): Response<PreparePaymentResponse, MercadoPagoError> {
        return preparePaymentRepository.prepare().also {
            it.resolve(
                success = {
                    // Check for status and do stuff to save discount token
                }
            )
        }
    }
}
