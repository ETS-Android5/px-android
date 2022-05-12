package com.mercadopago.android.px.internal.datasource

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.addons.TokenDeviceBehaviour
import com.mercadopago.android.px.model.internal.CardStatusDM

internal class CardStatusRepositoryImpl(
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val tokenDeviceBehaviour: TokenDeviceBehaviour
) : CardStatusRepository {

    override fun getCardsStatus(): List<CardStatusDM> {
        val cardsWithEsc = escManagerBehaviour.escCardIds
        val tokensStatus = tokenDeviceBehaviour.tokensStatus
        val allCardIds = cardsWithEsc + tokensStatus.map { it.cardId }
        return mutableListOf<CardStatusDM>().also {
            allCardIds.forEach { cardId ->
                val tokenData = tokensStatus.firstOrNull { it.cardId == cardId }
                it.add(
                    CardStatusDM(
                        cardId = cardId,
                        tokenInfo = CardStatusDM.TokenInfo.from(tokenData),
                        hasEsc = cardsWithEsc.contains(cardId)
                    )
                )
            }
        }
    }
}
