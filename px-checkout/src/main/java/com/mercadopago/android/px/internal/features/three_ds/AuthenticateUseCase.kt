package com.mercadopago.android.px.internal.features.three_ds

import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.CardHolderAuthenticatorRepository
import com.mercadopago.android.px.internal.util.ThreeDSWrapper
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.tracking.internal.MPTracker


internal class AuthenticateUseCase @JvmOverloads constructor(
    tracker: MPTracker,
    private val threeDSWrapper: ThreeDSWrapper,
    private val cardHolderAuthenticatorRepository: CardHolderAuthenticatorRepository,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<PaymentData, Any>(tracker) {

    override suspend fun doExecute(param: PaymentData): Response<Any, MercadoPagoError> {
        val response = cardHolderAuthenticatorRepository.authenticate(
            param,
            threeDSWrapper.getAuthenticationParameters()
        )

        return Response.Success(response)
    }
}