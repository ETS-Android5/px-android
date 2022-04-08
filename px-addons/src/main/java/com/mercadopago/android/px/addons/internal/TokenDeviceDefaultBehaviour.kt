package com.mercadopago.android.px.addons.internal

import com.mercadopago.android.px.addons.TokenDeviceBehaviour
import com.mercadopago.android.px.addons.tokenization.Tokenize
import com.mercadopago.android.px.addons.model.RemotePaymentToken
import com.mercadopago.android.px.addons.model.TokenState
import java.math.BigDecimal

internal class TokenDeviceDefaultBehaviour : TokenDeviceBehaviour {
    override val isFeatureAvailable: Boolean = false
    override val tokensStatus: List<TokenState> = listOf()
    override fun getTokenize(
        flowId: String,
        cardId: String,
        vProvisionedTokenId: String?
    ) = Tokenize()
    override fun getTokenStatus(cardId: String) = TokenState(cardId, TokenState.State.NONE)
    override suspend fun getRemotePaymentToken(
        cardId: String,
        amount: BigDecimal,
        vProvisionedTokenId: String?
    ): RemotePaymentToken {
        TODO("Not yet implemented")
    }
}
