package com.mercadopago.android.px.internal.base.use_case

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.features.validation_program.TokenDeviceUseCase
import com.mercadopago.android.px.internal.repository.TokenRepository
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.exceptions.ApiException
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.services.Callback
import com.mercadopago.android.px.tracking.internal.MPTracker
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class TokenizeWithoutCvvUseCase(
    private val tokenDeviceUseCase: TokenDeviceUseCase,
    private val tokenRepository: TokenRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Card, Token>(tracker) {

    override suspend fun doExecute(param: Card): Response<Token, MercadoPagoError> {
        return suspendCoroutine { continuation ->
            tokenDeviceUseCase.execute(
                TokenDeviceUseCase.createParams(param),
                success = { remotePaymentToken ->
                    tokenRepository.createTokenWithoutCvv(param, remotePaymentToken).enqueue(object : Callback<Token>() {
                        override fun success(token: Token) {
                            continuation.resume(Response.Success(token))
                        }

                        override fun failure(apiException: ApiException) {
                            continuation.resume(Response.Failure(MercadoPagoError(apiException, ApiUtil.RequestOrigin.CREATE_TOKEN)))
                        }
                    })
                },
                failure = {
                    continuation.resume(Response.Failure(it))
                }
            )
        }

    }
}
