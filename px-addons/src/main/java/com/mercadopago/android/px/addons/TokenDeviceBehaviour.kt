package com.mercadopago.android.px.addons

import com.mercadopago.android.px.addons.tokenization.Tokenize
import com.mercadopago.android.px.addons.model.RemotePaymentToken
import com.mercadopago.android.px.addons.model.TokenState
import java.math.BigDecimal

interface TokenDeviceBehaviour {

    val isFeatureAvailable: Boolean

    val tokensStatus: List<TokenState>

    fun getTokenStatus(cardId: String): TokenState

    fun getTokenize(
        flowId: String,
        cardId: String,
        vProvisionedTokenId: String? = null
    ): Tokenize

    suspend fun getRemotePaymentToken(
        cardId: String,
        amount: BigDecimal,
        vProvisionedTokenId: String? = null
    ): RemotePaymentToken
}
