package com.mercadopago.android.px.internal.base.use_case

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.features.validation_program.TokenDeviceUseCase
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.internal.util.ApiUtil
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.exceptions.ApiException
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.services.Callback
import com.mercadopago.android.px.tracking.internal.MPTracker
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class TokenizeWithCvvUseCase(
    private val tokenDeviceUseCase: TokenDeviceUseCase,
    private val cardTokenRepository: CardTokenRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<TokenizeWithCvvUseCase.Params, Token>(tracker) {

    override suspend fun doExecute(param: Params): Response<Token, MercadoPagoError> {
        return suspendCoroutine { continuation ->
            val tokenDeviceParams = TokenDeviceUseCase.buildParams(param.card)
            tokenDeviceUseCase.execute(
                tokenDeviceParams,
                success = { remotePaymentToken ->
                    cardTokenRepository.createToken(tokenDeviceParams.cardId, param.cvv, remotePaymentToken, param.requireEsc).enqueue(object : Callback<Token>() {
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

    data class Params(val card: Card, val cvv: String, val requireEsc: Boolean)
}
