package com.mercadopago.android.px.internal.util

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.addons.model.EscDeleteReason
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithCvvUseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.extensions.ifSuccess
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.SavedESCCardToken
import com.mercadopago.android.px.model.CardToken
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.exceptions.CardTokenException
import com.mercadopago.android.px.internal.helper.SecurityCodeHelper
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.tracking.internal.model.Reason

internal class TokenCreationWrapper private constructor(builder: Builder) {

    private val cardTokenRepository = builder.cardTokenRepository
    private val escManagerBehaviour = builder.escManagerBehaviour
    private val card = builder.card
    private val token = builder.token
    private val paymentMethod = builder.paymentMethod
    private val reason = builder.reason

    suspend fun createTokenWithEsc(esc: String): Response<Token, MercadoPagoError> {
        val cardId = if (card != null) card.id!! else token!!.cardId
        val body = SavedESCCardToken.createWithEsc(cardId, esc)

        return createESCToken(body)
    }

    suspend fun createTokenWithCvv(cvv: String): Response<Token, MercadoPagoError> {
        val body = SavedESCCardToken.createWithSecurityCode(card!!.id.orEmpty(), cvv)
            .also { it.validateSecurityCode(card) }

        return createESCToken(body)
    }

    suspend fun createTokenWithoutCvv(): Response<Token, MercadoPagoError> {
        val body = SavedESCCardToken.createWithoutSecurityCode(card!!.id.orEmpty())

        return createESCToken(body)
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
