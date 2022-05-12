package com.mercadopago.android.px.internal.base.use_case

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.extensions.notNull
import com.mercadopago.android.px.internal.extensions.runIfNotNull
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.util.CVVRecoveryWrapper
import com.mercadopago.android.px.internal.util.TokenCreationWrapper
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.tracking.internal.MPTracker

internal class TokenizeUseCase(
    private val tokenizeWithCvvUseCase: TokenizeWithCvvUseCase,
    private val cardTokenRepository: CardTokenRepository,
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val paymentSettingRepository: PaymentSettingRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<TokenizeParams, Token>(tracker) {

    override suspend fun doExecute(param: TokenizeParams) = run {
        param.paymentRecovery.runIfNotNull {
            CVVRecoveryWrapper(
                cardTokenRepository,
                escManagerBehaviour,
                it,
                tracker
            ).recoverWithCVV(param.securityCode)
        } ?: notNull(param.card).let {
            TokenCreationWrapper
                .Builder(cardTokenRepository, escManagerBehaviour)
                .with(it)
                .build()
                .createToken(param.securityCode)
        }
    }.also { it.resolve(success = { token -> paymentSettingRepository.configure(token) }) }
}

data class TokenizeParams(val securityCode: String, val card: Card, val paymentRecovery: PaymentRecovery? = null)
