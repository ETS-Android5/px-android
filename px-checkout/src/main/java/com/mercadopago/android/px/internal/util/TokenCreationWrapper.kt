package com.mercadopago.android.px.internal.util

import com.google.gson.internal.`$Gson$Types`.resolve
import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.addons.model.EscDeleteReason
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithCvvUseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.exceptions.CardTokenException
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

    suspend fun cloneToken(cvv: String) = when (val response = doCloneToken()) {
        is Response.Success -> putCVV(cvv, response.result.id)
        is Response.Failure -> response
    }

    @Throws(CardTokenException::class)
    fun validateCVVFromToken(cvv: String): Boolean {
        if (token?.firstSixDigits.isNotNullNorEmpty()) {
            CardToken.validateSecurityCode(cvv, paymentMethod, token!!.firstSixDigits)
        } else if (!CardToken.validateSecurityCode(cvv)) {
            throw CardTokenException(CardTokenException.INVALID_FIELD)
        }
        return true
    }

    suspend fun createTokenWithCvv(cvv: String): Response<Token, MercadoPagoError> {
        val body = SavedESCCardToken.createWithSecurityCode(card!!.id.orEmpty(), cvv)
            .also { it.validateSecurityCode(card) }

        return createESCToken(body)
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
        val escManagerBehaviour: ESCManagerBehaviour
    ) {
        var card: Card? = null
            private set

        var token: Token? = null
            private set

        var paymentMethod: PaymentMethod? = null
            private set

        var reason: Reason? = Reason.NO_REASON
            private set

        fun with(card: Card) = apply {
            this.card = card
        }
        fun with(paymentMethod: PaymentMethod) = apply { this.paymentMethod = paymentMethod }
        fun with(paymentRecovery: PaymentRecovery) = apply {
            card = paymentRecovery.card!!
            reason = Reason.from(paymentRecovery)
        }

        fun build() = TokenCreationWrapper(this)
    }
}
