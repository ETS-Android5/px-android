package com.mercadopago.android.px.internal.mapper

import com.mercadopago.android.px.assertEquals
import com.mercadopago.android.px.internal.mappers.PreparePaymentBodyMapper
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.ChargeRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.Issuer
import com.mercadopago.android.px.model.PaymentMethod
import com.mercadopago.android.px.model.PaymentMethods
import com.mercadopago.android.px.model.PaymentTypes
import com.mercadopago.android.px.preferences.CheckoutPreference
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import java.math.BigDecimal
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PreparePaymentBodyMapperTests {

    private lateinit var preparePaymentBodyMapper: PreparePaymentBodyMapper

    @RelaxedMockK
    private lateinit var paymentSettingRepository: PaymentSettingRepository

    @MockK
    private lateinit var userSelectionRepository: UserSelectionRepository

    @RelaxedMockK
    private lateinit var payerPaymentMethodRepository: PayerPaymentMethodRepository

    @RelaxedMockK
    private lateinit var amountConfigurationRepository: AmountConfigurationRepository

    @RelaxedMockK
    private lateinit var discountRepository: DiscountRepository

    @RelaxedMockK
    private lateinit var chargeRepository: ChargeRepository

    @RelaxedMockK
    private lateinit var preference: CheckoutPreference

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { paymentSettingRepository.checkoutPreference } returns preference
        every { preference.totalAmount } returns BigDecimal.TEN
        every { userSelectionRepository.customOptionId } returns "1234"
        preparePaymentBodyMapper = PreparePaymentBodyMapper(
            paymentSettingRepository,
            userSelectionRepository,
            payerPaymentMethodRepository,
            amountConfigurationRepository,
            discountRepository,
            chargeRepository
        )
    }

    /**
     * Case regular payment method (account_money) with no discounts
     */
    @Test
    fun `given an account money payment with no discounts when i map the body then it should contain account money info`() {
        every { userSelectionRepository.paymentMethod } returns PaymentMethod(
            PaymentMethods.ACCOUNT_MONEY, null, PaymentTypes.ACCOUNT_MONEY
        )
        every { amountConfigurationRepository.getCurrentConfiguration().splitConfiguration } returns null
        every { userSelectionRepository.secondaryPaymentMethod } returns null
        every { userSelectionRepository.card } returns null
        every { discountRepository.getCurrentConfiguration().discount } returns null
        val body = preparePaymentBodyMapper.map()
        with(body.paymentMethod) {
            amount!!.assertEquals(BigDecimal.TEN)
            id.assertEquals(PaymentMethods.ACCOUNT_MONEY)
            paymentTypeId.assertEquals(PaymentTypes.ACCOUNT_MONEY)
            assertNull(cardInfo)
            splitPaymentMethods!!.size.assertEquals(0)
            assertNull(discountInfo)
        }
    }

    /**
     * Case regular payment method (debit_card) with no discounts
     */
    @Test
    fun `given an debit card payment with no discounts when i map the body then it should contain debit card info`() {
        every { userSelectionRepository.paymentMethod } returns PaymentMethod(
            PaymentMethods.ARGENTINA.VISA, null, PaymentTypes.DEBIT_CARD
        )
        every { amountConfigurationRepository.getCurrentConfiguration().splitConfiguration } returns null
        every { userSelectionRepository.secondaryPaymentMethod } returns null
        every { userSelectionRepository.card } returns Card().also {
            it.id = "1234"
            it.issuer = Issuer(12345L, "test")
            it.firstSixDigits = "123456"
        }
        every { discountRepository.getCurrentConfiguration().discount } returns null
        val body = preparePaymentBodyMapper.map()
        with(body.paymentMethod) {
            amount!!.assertEquals(BigDecimal.TEN)
            id.assertEquals(PaymentMethods.ARGENTINA.VISA)
            paymentTypeId.assertEquals(PaymentTypes.DEBIT_CARD)
            assertNotNull(cardInfo)
            cardInfo!!.id.assertEquals("1234")
            cardInfo!!.issuerId.assertEquals(12345L)
            cardInfo!!.bin.assertEquals("123456")
            splitPaymentMethods!!.size.assertEquals(0)
            assertNull(discountInfo)
        }
    }
}
