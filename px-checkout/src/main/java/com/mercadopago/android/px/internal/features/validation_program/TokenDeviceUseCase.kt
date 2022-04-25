package com.mercadopago.android.px.internal.features.validation_program

import com.mercadopago.android.px.addons.TokenDeviceBehaviour
import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.model.RemotePaymentToken
import com.mercadopago.android.px.internal.repository.AmountRepository
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.Application.KnownValidationProgram
import com.mercadopago.android.px.tracking.internal.MPTracker

private typealias CardId = String
private typealias VProvisionedTokenId = String?

internal class TokenDeviceUseCase(
    private val amountRepository: AmountRepository,
    private val tokenDeviceBehaviour: TokenDeviceBehaviour,
    private val applicationSelectionRepository: ApplicationSelectionRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<TokenDeviceUseCase.Params, RemotePaymentToken?>(tracker) {

    override suspend fun doExecute(param: Params): Response<RemotePaymentToken?, MercadoPagoError> {
        val validationProgram = applicationSelectionRepository[param.cardId]
            .validationPrograms?.firstOrNull { it.status.enabled }
        val knownValidationProgram = KnownValidationProgram[validationProgram?.id]
        var result: RemotePaymentToken? = null

        if (knownValidationProgram == KnownValidationProgram.TOKEN_DEVICE) {
            result = with(tokenDeviceBehaviour.getRemotePaymentToken(
                cardId = param.cardId,
                amount = amountRepository.currentAmountToPay,
                vProvisionedTokenId = param.vProvisionedTokenId
            )) {
                RemotePaymentToken(cryptogramData, digitalPan, par, digitalPanExpirationDate)
            }
        }

        return Response.Success(result)
    }

    data class Params(val cardId: CardId, val vProvisionedTokenId: VProvisionedTokenId = null)

    companion object {
        fun buildParams(card: Card): Params {
            return Params(
                cardId = card.id ?: throw IllegalStateException("Cannot tokenize a card without id"),
                vProvisionedTokenId = card.vProvisionedTokenId
            )
        }
    }
}
