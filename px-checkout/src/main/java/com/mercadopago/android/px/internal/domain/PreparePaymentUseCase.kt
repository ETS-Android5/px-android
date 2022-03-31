package com.mercadopago.android.px.internal.domain

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.extensions.ifSuccess
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.ResponseSectionStatus
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.tracking.internal.MPTracker

private const val SECTION_DISCOUNTS = "discounts"

internal class PreparePaymentUseCase(
    private val preparePaymentRepository: PreparePaymentRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Unit, PreparePaymentResponse>(tracker) {
    override suspend fun doExecute(param: Unit): Response<PreparePaymentResponse, MercadoPagoError> {
        return preparePaymentRepository.prepare().ifSuccess {
            handleDiscountsResponse(it)
        }
    }

    private fun handleDiscountsResponse(response: PreparePaymentResponse) {
        val status = response.status[SECTION_DISCOUNTS]
        when(status?.code) {
            ResponseSectionStatus.Code.OK -> response.paymentMethod.discountInfo?.discountToken
            ResponseSectionStatus.Code.NON_RECOVERABLE -> status.message
            else -> return // Empty status for discounts means we don't have to do anything
        }
    }
}
