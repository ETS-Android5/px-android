package com.mercadopago.android.px.internal.usecases

import com.mercadopago.android.px.CallbackTest
import com.mercadopago.android.px.TestContextProvider
import com.mercadopago.android.px.assertEquals
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository.PaymentDiscounts
import com.mercadopago.android.px.internal.domain.GeneratePreparePaymentBodyUseCase
import com.mercadopago.android.px.internal.domain.PreparePaymentUseCase
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.AmountConfiguration
import com.mercadopago.android.px.model.Discount
import com.mercadopago.android.px.model.DiscountConfigurationModel
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.PaymentMethodConfiguration
import com.mercadopago.android.px.model.PaymentMethods
import com.mercadopago.android.px.model.PaymentTypes
import com.mercadopago.android.px.model.Split
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.exceptions.PreparePaymentMismatchError
import com.mercadopago.android.px.model.internal.ResponseSectionStatus
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse.Companion.SECTION_DISCOUNTS
import com.mercadopago.android.px.tracking.internal.MPTracker
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.verify
import java.math.BigDecimal
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PreparePaymentUseCaseTest {

    private lateinit var preparePaymentUseCase: PreparePaymentUseCase

    @RelaxedMockK
    private lateinit var success: CallbackTest<Unit>

    @RelaxedMockK
    private lateinit var failure: CallbackTest<MercadoPagoError>

    @MockK
    private lateinit var preparePaymentRepository: PreparePaymentRepository

    @RelaxedMockK
    private lateinit var paymentDiscountRepository: PaymentDiscountRepository

    @MockK
    private lateinit var discountRepository: DiscountRepository

    @RelaxedMockK
    private lateinit var userSelectionRepository: UserSelectionRepository

    @RelaxedMockK
    private lateinit var amountConfigurationRepository: AmountConfigurationRepository

    @MockK
    private lateinit var tracker: MPTracker

    @MockK
    private lateinit var discountConfigurationModel: DiscountConfigurationModel

    @MockK
    private lateinit var generatePreparePaymentBodyUseCase: GeneratePreparePaymentBodyUseCase

    @RelaxedMockK
    private lateinit var secondaryPaymentMethod: PaymentMethod

    @RelaxedMockK
    private lateinit var amountConfiguration: AmountConfiguration

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        preparePaymentUseCase = PreparePaymentUseCase(
            preparePaymentRepository,
            paymentDiscountRepository,
            discountRepository,
            amountConfigurationRepository,
            userSelectionRepository,
            generatePreparePaymentBodyUseCase,
            tracker,
            TestContextProvider()
        )
        every { amountConfigurationRepository.getCurrentConfiguration() } returns amountConfiguration
        coEvery { generatePreparePaymentBodyUseCase.execute(any()) } returns Response.Success(mockk())
    }

    //region No Discount Tests

    /**
     * Case with split and no discounts with empty response
     */
    @Test
    fun `given it's split and we don't have discounts when response is empty then it should only reset discounts`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod
        val response = PreparePaymentResponse(null, null)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        verify {
            paymentDiscountRepository.reset()
        }
        verify(exactly = 0) {
            paymentDiscountRepository.configure(any())
        }
    }

    /**
     * Case with regular payment method and no discounts with empty response
     */
    @Test
    fun `given it's a regular pm and we don't have discounts when response is empty then it should only reset discounts`() {
        every { amountConfiguration.splitConfiguration } returns null
        every { discountConfigurationModel.discount } returns null
        every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
        every { amountConfiguration.discountToken } returns "hash_no_discount"
        every { userSelectionRepository.secondaryPaymentMethod } returns null
        val response = PreparePaymentResponse(null, null)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        verify {
            paymentDiscountRepository.reset()
        }
        verify(exactly = 0) {
            paymentDiscountRepository.configure(any())
        }
    }

    //endregion

    //region Status OK Tests
    /**
     * Case with one payment method with discount and status OK
     */
    @Test
    fun `given it's a regular pm and has discount when status is OK then it should configure merged discount from discount repository`() {
        val paymentMethodParams = getAccountMoneyPaymentMethodParams()
        with(paymentMethodParams) {
            val response = getPreparePaymentResponse(paymentMethodParams, null, ResponseSectionStatus.Code.OK)
            coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
            every { discountConfigurationModel.discount } returns checkoutInitDiscount
            every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
            every { amountConfiguration.discountToken } returns "00000"
            every { userSelectionRepository.secondaryPaymentMethod } returns null

            preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

            verify {
                success.invoke(Unit)
            }
            val configuredDiscounts = slot<PaymentDiscounts>()
            verify {
                paymentDiscountRepository.configure(capture(configuredDiscounts))
            }
            with(configuredDiscounts.captured) {
                val expectedMergedDiscount = Discount.replaceWith(checkoutInitDiscount, preparePaymentToken)
                assertNotNull(primaryPaymentMethodDiscount)
                primaryPaymentMethodDiscount!!.assertEquals(expectedMergedDiscount!!)

                assertNull(splitPaymentMethodDiscount)
            }
        }
    }

    /**
     * Case with split payment with discount for both payment methods and status OK
     */
    @Test
    fun `given it's split with both discounts when status is OK then it should configure merged discounts from split configuration`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams()
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams()
        val response = getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.OK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedMergedPrimaryPaymentMethodDiscount =
                with(primaryPaymentMethodParams) { Discount.replaceWith(checkoutInitDiscount, preparePaymentToken) }
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedMergedPrimaryPaymentMethodDiscount!!)

            val expectedMergedSplitPaymentMethodDiscount =
                with(splitPaymentMethodParams) { Discount.replaceWith(checkoutInitDiscount, preparePaymentToken) }
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedMergedSplitPaymentMethodDiscount!!)
        }
    }

    /**
     * Case with split payment and discount only for primary payment method and status OK
     */
    @Test
    fun `given it's split with discount for primary method when status is OK then it should configure merged discount from split configuration`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams()
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withCheckoutDiscount = false, withPreparePaymentToken = false)
        val response = getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.OK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedMergedPrimaryPaymentMethodDiscount =
                with(primaryPaymentMethodParams) { Discount.replaceWith(checkoutInitDiscount, preparePaymentToken) }
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedMergedPrimaryPaymentMethodDiscount!!)

            assertNull(splitPaymentMethodDiscount)
        }
    }

    /**
     * Case with split payment and discount only for split payment method and status OK
     */
    @Test
    fun `given it's split with discount for split method when status is OK then it should configure merged discount from split configuration`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams()
        val response = getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.OK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            assertNull(primaryPaymentMethodDiscount)

            val expectedMergedSplitPaymentMethodDiscount =
                with(splitPaymentMethodParams) { Discount.replaceWith(checkoutInitDiscount, preparePaymentToken) }
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedMergedSplitPaymentMethodDiscount!!)
        }
    }

    //endregion

    //region Status USE FALLBACK Tests

    /**
     * Case with one payment method with discount and status USE FALLBACK
     */
    @Test
    fun `given it's a regular pm and has discount when status is USE FALLBACK then it should configure discount from discount repository with checkout init token`() {
        val paymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val checkoutInitToken = "checkout INIT token"
        with(paymentMethodParams) {
            val response =
                getPreparePaymentResponse(paymentMethodParams, null, ResponseSectionStatus.Code.USE_FALLBACK)
            coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
            every { discountConfigurationModel.discount } returns checkoutInitDiscount
            every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
            every { amountConfiguration.discountToken } returns checkoutInitToken
            every { userSelectionRepository.secondaryPaymentMethod } returns null

            preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

            verify {
                success.invoke(Unit)
            }
            val configuredDiscounts = slot<PaymentDiscounts>()
            verify {
                paymentDiscountRepository.configure(capture(configuredDiscounts))
            }
            with(configuredDiscounts.captured) {
                val expectedDiscount = Discount.replaceWith(checkoutInitDiscount, checkoutInitToken)
                assertNotNull(primaryPaymentMethodDiscount)
                primaryPaymentMethodDiscount!!.assertEquals(expectedDiscount!!)

                assertNull(splitPaymentMethodDiscount)
            }
        }
    }

    /**
     * Case with split payment, discount for both payment methods and status USE FALLBACK
     */
    @Test
    fun `given it's split with both discounts when status is USE FALLBACK then it should configure discounts from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val response =
            getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.USE_FALLBACK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedPrimaryPaymentMethodDiscount = primaryPaymentMethodParams.checkoutInitDiscount
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedPrimaryPaymentMethodDiscount!!)

            val expectedMergedSplitPaymentMethodDiscount = splitPaymentMethodParams.checkoutInitDiscount
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedMergedSplitPaymentMethodDiscount!!)
        }
    }

    /**
     * Case with split payment, discount only for primary payment method and status USE FALLBACK
     */
    @Test
    fun `given it's split with discount for primary method when status is USE FALLBACK then it should configure primary method discount from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withCheckoutDiscount = false, withPreparePaymentToken = false)
        val response =
            getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.USE_FALLBACK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedPrimaryPaymentMethodDiscount = primaryPaymentMethodParams.checkoutInitDiscount
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedPrimaryPaymentMethodDiscount!!)

            assertNull(splitPaymentMethodDiscount)
        }
    }

    /**
     * Case with split payment, discount only for split payment method and status USE FALLBACK
     */
    @Test
    fun `given it's split with discount for split method when status is USE FALLBACK then it should configure split method discount from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val response =
            getPreparePaymentResponse(primaryPaymentMethodParams, splitPaymentMethodParams, ResponseSectionStatus.Code.USE_FALLBACK)
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            success.invoke(Unit)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            assertNull(primaryPaymentMethodDiscount)

            val expectedSplitPaymentMethodDiscount = splitPaymentMethodParams.checkoutInitDiscount
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedSplitPaymentMethodDiscount!!)
        }
    }

    //endregion

    //region Status MISMATCH Tests

    @Test
    fun `given we request a prepare payment when status is MISTMATCH then it should not configure discounts and return recoverable error`() {
        val paymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val checkoutInitToken = "checkout INIT token"
        val statusMessage = "Error ocurred so we need to reload checkout"
        with(paymentMethodParams) {
            val response =
                getPreparePaymentResponse(paymentMethodParams, null, ResponseSectionStatus.Code.MISMATCH, statusMessage)
            coEvery { preparePaymentRepository.prepare(any()) } returns Response.Success(response)
            every { discountConfigurationModel.discount } returns checkoutInitDiscount
            every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
            every { amountConfiguration.discountToken } returns checkoutInitToken
            every { userSelectionRepository.secondaryPaymentMethod } returns null

            preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

            verify(exactly = 0) {
                paymentDiscountRepository.configure(any())
            }
            val error = slot<MercadoPagoError>()
            verify {
                failure.invoke(capture(error))
            }
            assertTrue(error.captured is PreparePaymentMismatchError)
            assertTrue(error.captured.isRecoverable)
            error.captured.message.assertEquals(statusMessage)
        }
    }

    //endregion

    // region API Error Tests

    /**
     * Case with split payment methods and no discounts when API fails
     */
    @Test
    fun `given it's split and we don't have discounts when api fails then it should configure null discounts`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val error = MercadoPagoError.createRecoverable("Test")
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            failure.invoke(error)
        }
        verify {
            paymentDiscountRepository.reset()
            paymentDiscountRepository.configure(
                PaymentDiscounts(null, null)
            )
        }
    }

    /**
     * Case with regular payment and no discounts when API fails
     */
    @Test
    fun `given it's a regular pm and we don't have discounts when api fails then it should configure null discounts`() {
        val error = MercadoPagoError.createRecoverable("Test")
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
        every { amountConfiguration.splitConfiguration } returns null
        every { discountConfigurationModel.discount } returns null
        every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
        every { amountConfiguration.discountToken } returns "hash_no_discount"
        every { userSelectionRepository.secondaryPaymentMethod } returns null

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            failure.invoke(error)
        }
        verify {
            paymentDiscountRepository.reset()
            paymentDiscountRepository.configure(
                PaymentDiscounts(null, null)
            )
        }
    }

    /**
     * Case with one payment method with discount when API fails
     */
    @Test
    fun `given it's a regular pm and has discount when api fails then it should configure discount from discount repository with checkout init token`() {
        val paymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val checkoutInitToken = "checkout INIT token"
        val error = MercadoPagoError.createRecoverable("Test")
        with(paymentMethodParams) {
            coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
            every { discountConfigurationModel.discount } returns checkoutInitDiscount
            every { discountRepository.getCurrentConfiguration() } returns discountConfigurationModel
            every { amountConfiguration.discountToken } returns checkoutInitToken
            every { userSelectionRepository.secondaryPaymentMethod } returns null

            preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

            verify {
                failure.invoke(error)
            }
            val configuredDiscounts = slot<PaymentDiscounts>()
            verify {
                paymentDiscountRepository.configure(capture(configuredDiscounts))
            }
            with(configuredDiscounts.captured) {
                val expectedDiscount = Discount.replaceWith(checkoutInitDiscount, checkoutInitToken)
                assertNotNull(primaryPaymentMethodDiscount)
                primaryPaymentMethodDiscount!!.assertEquals(expectedDiscount!!)

                assertNull(splitPaymentMethodDiscount)
            }
        }
    }

    /**
     * Case with split payment and discount for both methods when API fails
     */
    @Test
    fun `given it's split with both discounts when api fails then it should configure discount from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val error = MercadoPagoError.createRecoverable("Test")
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            failure.invoke(error)
        }
        val configuredDiscounts = slot<PaymentDiscounts>()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedPrimaryPaymentMethodDiscount = primaryPaymentMethodParams.checkoutInitDiscount
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedPrimaryPaymentMethodDiscount!!)

            val expectedMergedSplitPaymentMethodDiscount = splitPaymentMethodParams.checkoutInitDiscount
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedMergedSplitPaymentMethodDiscount!!)
        }
    }

    /**
     * Case with split payment and discount only for primary payment method when API fails
     */
    @Test
    fun `given it's split with discount for primary method when api fails then it should configure primary method discount from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withCheckoutDiscount = false, withPreparePaymentToken = false)
        val error = MercadoPagoError.createRecoverable("Test")
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            failure.invoke(error)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            val expectedPrimaryPaymentMethodDiscount = primaryPaymentMethodParams.checkoutInitDiscount
            assertNotNull(primaryPaymentMethodDiscount)
            primaryPaymentMethodDiscount!!.assertEquals(expectedPrimaryPaymentMethodDiscount!!)

            assertNull(splitPaymentMethodDiscount)
        }
    }

    /**
     * Case with split payment, discount only for split payment method when API fails.
     */
    @Test
    fun `given it's split with discount for split method when api fails then it should configure split method discount from split configuration without merging`() {
        val primaryPaymentMethodParams = getDebitPaymentMethodParams(withPreparePaymentToken = false, withCheckoutDiscount = false)
        val splitPaymentMethodParams = getAccountMoneyPaymentMethodParams(withPreparePaymentToken = false)
        val error = MercadoPagoError.createRecoverable("Test")
        coEvery { preparePaymentRepository.prepare(any()) } returns Response.Failure(error)
        setupSplitConfigurationMock(primaryPaymentMethodParams, splitPaymentMethodParams)
        every { userSelectionRepository.secondaryPaymentMethod } returns secondaryPaymentMethod

        preparePaymentUseCase.execute(Unit, success::invoke, failure::invoke)

        verify {
            failure.invoke(error)
        }
        val configuredDiscounts: CapturingSlot<PaymentDiscounts> = slot()
        verify {
            paymentDiscountRepository.configure(capture(configuredDiscounts))
        }
        with(configuredDiscounts.captured) {
            assertNull(primaryPaymentMethodDiscount)

            val expectedSplitPaymentMethodDiscount = splitPaymentMethodParams.checkoutInitDiscount
            assertNotNull(splitPaymentMethodDiscount)
            splitPaymentMethodDiscount!!.assertEquals(expectedSplitPaymentMethodDiscount!!)
        }
    }

    // endregion

    private fun setupSplitConfigurationMock(
        primaryPaymentMethodParams: PaymentMethodParameters,
        splitPaymentMethodParams: PaymentMethodParameters
    ) {
        Split().also { split ->
            split.primaryPaymentMethod = PaymentMethodConfiguration().also { it.discount = primaryPaymentMethodParams.checkoutInitDiscount }
            split.secondaryPaymentMethod = PaymentMethodConfiguration().also { it.discount = splitPaymentMethodParams.checkoutInitDiscount }
            every { amountConfiguration.splitConfiguration } returns split
        }
    }

    private fun getPreparePaymentResponse(
        primaryPaymentMethodParams: PaymentMethodParameters,
        splitPaymentMethodParams: PaymentMethodParameters?,
        responseSectionCode: ResponseSectionStatus.Code,
        statusMessage: String? = null
    ) = PreparePaymentResponse(
        mapOf(getDiscountStatus(responseSectionCode, statusMessage)),
        with(primaryPaymentMethodParams) {
            getPaymentMethodDM(id, type, token = preparePaymentToken, splitPaymentMethods = splitPaymentMethodParams?.let {
                listOf(
                    getPaymentMethodDM(it.id, it.type, token = it.preparePaymentToken)
                )
            })
        }
    )

    private fun getDebitPaymentMethodParams(withPreparePaymentToken: Boolean = true, withCheckoutDiscount: Boolean = true) =
        PaymentMethodParameters(
            PaymentMethods.ARGENTINA.VISA,
            PaymentTypes.DEBIT_CARD,
            if (withPreparePaymentToken) "new DEBIT token" else null,
            if (withCheckoutDiscount) {
                getDiscountMock("checkout DEBIT token", "discount", "ARS", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN)
            } else null
        )

    private fun getAccountMoneyPaymentMethodParams(withPreparePaymentToken: Boolean = true, withCheckoutDiscount: Boolean = true) =
        PaymentMethodParameters(
            PaymentMethods.ACCOUNT_MONEY,
            PaymentTypes.ACCOUNT_MONEY,
            if (withPreparePaymentToken) "new AM token" else null,
            if (withCheckoutDiscount) {
                getDiscountMock("checkout AM token", "discount", "ARS", BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN)
            } else null
        )

    private fun getPaymentMethodDM(
        id: String,
        type: String,
        amount: BigDecimal = BigDecimal.TEN,
        cardInfo: PaymentMethodDM.CardInfo? = null,
        source: List<String> = emptyList(),
        splitPaymentMethods: List<PaymentMethodDM>? = null,
        token: String? = null
    ) =
        PaymentMethodDM(id, type, amount, cardInfo, source, splitPaymentMethods, token?.let { PaymentMethodDM.DiscountInfo(token = it) })

    private fun getDiscountStatus(code: ResponseSectionStatus.Code, message: String?) =
        Pair(SECTION_DISCOUNTS, ResponseSectionStatus(code, message))

    private fun getDiscountMock(
        id: String,
        name: String,
        currencyId: String,
        percentOff: BigDecimal,
        amountOff: BigDecimal,
        couponAmount: BigDecimal
    ) =
        mockkClass(Discount::class).also {
            every { it.id } returns id
            every { it.name } returns name
            every { it.currencyId } returns currencyId
            every { it.percentOff } returns percentOff
            every { it.amountOff } returns amountOff
            every { it.couponAmount } returns couponAmount
        }

    data class PaymentMethodParameters(
        val id: String,
        val type: String,
        val preparePaymentToken: String?,
        val checkoutInitDiscount: Discount? = null,
        val key: PayerPaymentMethodKey = PayerPaymentMethodKey(id, type)
    )
}
