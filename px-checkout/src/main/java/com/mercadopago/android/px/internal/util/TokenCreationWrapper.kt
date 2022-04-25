package com.mercadopago.android.px.internal.util

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.addons.model.EscDeleteReason
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithCvvUseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.helper.SecurityCodeHelper
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.tracking.internal.model.Reason

internal class TokenCreationWrapper private constructor(builder: Builder) {

    private val cardTokenRepository: CardTokenRepository = builder.cardTokenRepository
    private val escManagerBehaviour: ESCManagerBehaviour = builder.escManagerBehaviour
    private val tokenizeWithCvvUseCase: TokenizeWithCvvUseCase = builder.tokenizeWithCvvUseCase
    private val card: Card = builder.card
    private val reason: Reason = builder.reason

    suspend fun createToken(cvv: String): Response<Token, MercadoPagoError> {
        return if (escManagerBehaviour.isESCEnabled) {
            createTokenWithEsc(cvv)
        } else {
            createTokenWithoutEsc(cvv)
        }
    }

    suspend fun createTokenWithEsc(cvv: String) = run {
        SecurityCodeHelper.validate(card, cvv)
        createESCToken(card, cvv).apply {
            resolve(success = { token -> token.lastFourDigits = card.lastFourDigits })
        }
    }

    suspend fun createTokenWithoutEsc(cvv: String) = run {
        SecurityCodeHelper.validate(card, cvv)
        createToken(card, cvv).apply {
            resolve(success = { token -> token.lastFourDigits = card.lastFourDigits })
        }
    }

    private suspend fun createESCToken(card: Card, cvv: String) = tokenizeWithCvvUseCase
        .suspendExecute(TokenizeWithCvvUseCase.Params(card, cvv, true)).apply {
            resolve(success = {
                val cardId = card.id ?: error("card id should not be null")
                if (Reason.ESC_CAP == reason) { // Remove previous esc for tracking purpose
                    escManagerBehaviour.deleteESCWith(cardId, EscDeleteReason.ESC_CAP, null)
                }
                cardTokenRepository.clearCap(cardId) {}
            })
        }

    private suspend fun createToken(card: Card, cvv: String) = tokenizeWithCvvUseCase
        .suspendExecute(TokenizeWithCvvUseCase.Params(card, cvv, false))

    class Builder(
        val cardTokenRepository: CardTokenRepository,
        val escManagerBehaviour: ESCManagerBehaviour,
        val tokenizeWithCvvUseCase: TokenizeWithCvvUseCase
    ) {
        lateinit var card: Card

        var reason: Reason = Reason.NO_REASON
            private set

        fun with(card: Card) = apply {
            this.card = card
        }

        fun with(paymentRecovery: PaymentRecovery) = apply {
            card = paymentRecovery.card!!
            reason = Reason.from(paymentRecovery)
        }

        fun build() = TokenCreationWrapper(this)
    }
}
