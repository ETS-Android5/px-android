package com.mercadopago.android.px.internal.usecases

import com.mercadopago.android.px.CallbackTest
import com.mercadopago.android.px.TestContextProvider
import com.mercadopago.android.px.internal.features.validation_program.AuthenticateUseCase
import com.mercadopago.android.px.internal.features.validation_program.ValidationProgramUseCase
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.Application
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker
import com.mercadopago.android.px.tracking.internal.events.ProgramValidationEvent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.math.BigDecimal

@RunWith(MockitoJUnitRunner::class)
class ValidationProgramUseCaseTest {

    @Mock
    private lateinit var success: CallbackTest<String?>

    @Mock
    private lateinit var failure: CallbackTest<MercadoPagoError>

    @Mock
    private lateinit var applicationSelectionRepository: ApplicationSelectionRepository

    @Mock
    private lateinit var authenticateUseCase: AuthenticateUseCase

    @Mock
    private lateinit var tracker: MPTracker

    private lateinit var validationProgramUseCase: ValidationProgramUseCase

    @Before
    fun setUp() {
        validationProgramUseCase = ValidationProgramUseCase(
            applicationSelectionRepository, authenticateUseCase, tracker, TestContextProvider())
    }

    @Test
    fun whenPaymentDataListIsNull() {
        validationProgramUseCase.execute(
            null,
            success::invoke,
            failure::invoke
        )

        verifyZeroInteractions(success)
        verify(tracker).track(any<FrictionEventTracker>())
        verify(failure).invoke(any())
    }

    @Test
    fun whenPaymentDataListIsEmpty() {
        validationProgramUseCase.execute(
            listOf(),
            success::invoke,
            failure::invoke
        )

        verifyZeroInteractions(success)
        verify(tracker).track(any<FrictionEventTracker>())
        verify(failure).invoke(any())
    }

    @Test
    fun whenIsNotKnownValidationProgram() {
        whenever(applicationSelectionRepository[any<String>()]).thenReturn(mock())

        validationProgramUseCase.execute(
            listOf(createPaymentData()),
            success::invoke,
            failure::invoke
        )

        verify(success).invoke(null)
        verify(tracker).track(any<ProgramValidationEvent>())
        verifyZeroInteractions(failure)
    }

    @Test
    fun whenIsSTPValidationProgram() {
        val status : Application.ValidationProgram.Status = mock {
            on { enabled }.thenReturn(true)
        }
        val validationProgram: Application.ValidationProgram = mock {
            on { id }.thenReturn("STP")
            on { this.status }.thenReturn(status)
        }
        val application: Application = mock {
            on { validationPrograms }.thenReturn(listOf(validationProgram))
        }
        whenever(applicationSelectionRepository[any<String>()]).thenReturn(application)

        validationProgramUseCase.execute(
            listOf(createPaymentData()),
            success::invoke,
            failure::invoke
        )

        verify(authenticateUseCase).execute(any(), any(), any())
        verify(success).invoke("stp")
        verify(tracker).track(any())
        verifyZeroInteractions(failure)
    }

    private fun createPaymentData(): PaymentData {
        val discount = mock<Discount>()
        val paymentMethod = mock<PaymentMethod> {
            on { id }.thenReturn("visa")
        }

        val token = mock<Token>()
        return PaymentData.Builder()
            .setToken(token)
            .setDiscount(discount)
            .setRawAmount(BigDecimal.TEN)
            .setPaymentMethod(paymentMethod)
            .createPaymentData()
    }

}