package com.mercadopago.android.px.securitycode

import com.mercadopago.android.px.CallbackTest
import com.mercadopago.android.px.TestContextProvider
import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.base.use_case.TokenizeParams
import com.mercadopago.android.px.internal.base.use_case.TokenizeUseCase
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithCvvUseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.repository.CardTokenRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.SecurityCode
import com.mercadopago.android.px.model.Token
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class TokenizeUseCaseTest {

    @Mock
    private lateinit var tokenizeWithCvvUseCase: TokenizeWithCvvUseCase

    @Mock
    private lateinit var cardTokenRepository: CardTokenRepository

    @Mock
    private lateinit var escManagerBehaviour: ESCManagerBehaviour

    @Mock
    private lateinit var settingRepository: PaymentSettingRepository

    @Mock
    private lateinit var success: CallbackTest<Token>

    @Mock
    private lateinit var failure: CallbackTest<MercadoPagoError>

    private lateinit var tokenizeUseCase: TokenizeUseCase
    private lateinit var contextProvider: TestContextProvider

    @Before
    fun setUp() {
        contextProvider = TestContextProvider()
        tokenizeUseCase = TokenizeUseCase(
            tokenizeWithCvvUseCase,
            cardTokenRepository,
            escManagerBehaviour,
            settingRepository,
            mock(),
            contextProvider)
    }

    @Test
    fun whenIsSecurityCodeAndSuccess() {
        val cardMock = Card().apply {
            id = "321"
        }
        val params = TokenizeParams("123", cardMock)
        val tokenMock = mock<Token>()
        runBlocking {
            whenever(tokenizeWithCvvUseCase.suspendExecute(any())).thenReturn(Response.Success(tokenMock))
        }

        tokenizeUseCase.execute(params, success::invoke, failure::invoke)

        runBlocking {
            verify(tokenizeWithCvvUseCase).suspendExecute(any())
        }
        verify(settingRepository).configure(tokenMock)
        verify(success).invoke(tokenMock)
        verifyNoInteractions(failure)
    }

    @Test
    fun whenIsSecurityCodeAndFail() {
        val params = TokenizeParams("123", mock())

        tokenizeUseCase.execute(params, success::invoke, failure::invoke)

        verify(failure).invoke(any())
        verifyNoInteractions(success)
    }

    @Test
    fun whenIsPaymentRecoveryAndSuccess() {
        val cvv = "123"
        val securityCode = mock<SecurityCode> {
            on { length }.thenReturn(cvv.length)
        }
        val cardMock = mock<Card> {
            on { id }.thenReturn("456")
            on { this.securityCode }.thenReturn(securityCode)
        }
        val paymentRecovery = mock<PaymentRecovery> {
            on { card }.thenReturn(cardMock)
        }
        val params = TokenizeParams(cvv, cardMock, paymentRecovery)
        val tokenResultMock = mock<Token>()
        runBlocking {
            whenever(tokenizeWithCvvUseCase.suspendExecute(any())).thenReturn(Response.Success(tokenResultMock))
        }
        whenever(escManagerBehaviour.isESCEnabled).thenReturn(true)

        tokenizeUseCase.execute(params, success::invoke, failure::invoke)

        runBlocking {
            verify(tokenizeWithCvvUseCase).suspendExecute(any())
        }
        verify(settingRepository).configure(tokenResultMock)
        verify(success).invoke(tokenResultMock)
        verifyNoInteractions(failure)
    }

    @Test
    fun whenIsPaymentRecoveryAndFail() {
        val params = TokenizeParams("123", mock(), mock())

        tokenizeUseCase.execute(params, success::invoke, failure::invoke)

        verify(failure).invoke(any())
        verifyNoInteractions(success)
    }
}
