package com.mercadopago.android.px.internal.util

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithCvvUseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.SecurityCode
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class TokenCreationWrapperTest {

    private lateinit var tokenCreationWrapperBuilder: TokenCreationWrapper.Builder

    @MockK private lateinit var cardTokenRepository: CardTokenRepository
    @MockK private lateinit var escManagerBehaviour: ESCManagerBehaviour
    @MockK private lateinit var tokenizeWithCvvUseCase: TokenizeWithCvvUseCase
    @MockK private lateinit var card: Card
    @MockK private lateinit var securityCode: SecurityCode
    private val cardId = "abc"
    private val vProvisionedTokenId = "vProvTokenID"

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { securityCode.cardLocation } returns "back"
        every { securityCode.length } returns 3
        every { card.id } returns cardId
        every { card.vProvisionedTokenId } returns vProvisionedTokenId
        every { card.lastFourDigits } returns "1234"
        every { card.securityCode } returns securityCode
        coEvery { tokenizeWithCvvUseCase.suspendExecute(any()) } returns Response.Success(mockk(relaxed = true))
        tokenCreationWrapperBuilder = TokenCreationWrapper.Builder(cardTokenRepository, escManagerBehaviour, tokenizeWithCvvUseCase)
    }

    @Test
    fun `given cvv when creating token with esc enabled then return token`() {
        val cvv = "123"
        val tokenizeParams = slot<TokenizeWithCvvUseCase.Params>()
        val clearCap = slot<CardTokenRepository.ClearCapCallback>()
        every { escManagerBehaviour.isESCEnabled } returns true
        every { cardTokenRepository.clearCap(cardId, capture(clearCap)) } returns Unit
        val tokenCreationWrapper = tokenCreationWrapperBuilder.with(card).build()

        runBlocking { tokenCreationWrapper.createToken(cvv) }
        clearCap.captured.execute()

        coVerify { tokenizeWithCvvUseCase.suspendExecute(capture(tokenizeParams)) }
        tokenizeParams.captured.let {
            Assert.assertEquals(it.cvv, cvv)
            Assert.assertEquals(it.card.id, cardId)
            Assert.assertEquals(it.card.vProvisionedTokenId, vProvisionedTokenId)
            Assert.assertEquals(it.requireEsc, true)
        }
    }

    @Test
    fun `given cvv when creating token with esc disabled then return token`() {
        val cvv = "123"
        val tokenizeParams = slot<TokenizeWithCvvUseCase.Params>()
        val tokenCreationWrapper = tokenCreationWrapperBuilder.with(card).build()
        every { escManagerBehaviour.isESCEnabled } returns false

        runBlocking { tokenCreationWrapper.createToken(cvv) }

        coVerify { tokenizeWithCvvUseCase.suspendExecute(capture(tokenizeParams)) }
        tokenizeParams.captured.let {
            Assert.assertEquals(it.cvv, cvv)
            Assert.assertEquals(it.card.id, cardId)
            Assert.assertEquals(it.card.vProvisionedTokenId, vProvisionedTokenId)
            Assert.assertEquals(it.requireEsc, false)
        }
    }
}
