package com.mercadopago.android.px.addons.model

import java.util.Date

data class TokenState(
    val cardId: String,
    val state: State,
    val vProvisionedTokenId: String? = null,
    val updatedAt: Date? = null
) {
    enum class State {
        ENABLED,
        IN_PROGRESS,
        SUSPENDED,
        DELETED,
        NONE
    }
}