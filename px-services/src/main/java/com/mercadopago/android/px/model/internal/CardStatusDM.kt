package com.mercadopago.android.px.model.internal

import com.mercadopago.android.px.addons.model.TokenState
import java.util.Date

data class CardStatusDM(val cardId: String, val tokenInfo: TokenInfo, val hasEsc: Boolean) {

    data class TokenInfo(
        val state: TokenStateDM = TokenStateDM.NONE,
        val vProvisionedTokenId: String? = null,
        val updatedAt: Date? = null
    ) {

        enum class TokenStateDM {
            ENABLED,
            IN_PROGRESS,
            SUSPENDED,
            DELETED,
            NONE;
        }

        companion object {
            fun from(tokenState: TokenState?): TokenInfo {
                return tokenState?.let {
                    val state = when (it.state) {
                        TokenState.State.ENABLED -> TokenStateDM.ENABLED
                        TokenState.State.IN_PROGRESS -> TokenStateDM.IN_PROGRESS
                        TokenState.State.DELETED -> TokenStateDM.DELETED
                        else -> TokenStateDM.NONE
                    }
                    TokenInfo(state, it.vProvisionedTokenId, it.updatedAt)
                } ?: TokenInfo()
            }
        }
    }
}