
package com.mercadopago.android.px.internal.mapper

import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.assertEquals
import com.mercadopago.android.px.configuration.AdvancedConfiguration
import com.mercadopago.android.px.configuration.DiscountParamsConfiguration
import com.mercadopago.android.px.configuration.PaymentConfiguration
import com.mercadopago.android.px.internal.datasource.CardStatusRepository
import com.mercadopago.android.px.internal.features.FeatureProvider
import com.mercadopago.android.px.internal.mappers.InitRequestBodyMapper
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.tracking.TrackingRepository
import com.mercadopago.android.px.model.PaymentTypes
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule
import com.mercadopago.android.px.model.internal.CardStatusDM
import com.mercadopago.android.px.model.internal.CheckoutFeatures
import com.mercadopago.android.px.model.internal.CheckoutType
import com.mercadopago.android.px.model.internal.PaymentTypeChargeRuleDM
import com.mercadopago.android.px.utils.StubCheckoutPreferenceUtils
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@RunWith(MockitoJUnitRunner::class)
class InitRequestBodyMapperTest {

    @Mock
    private lateinit var escManagerBehaviour: ESCManagerBehaviour

    @Mock
    private lateinit var featureProvider: FeatureProvider

    @Mock
    private lateinit var trackingRepository: TrackingRepository

    @Mock
    private lateinit var paymentSettingRepository: PaymentSettingRepository

    @Mock
    private lateinit var paymentConfiguration: PaymentConfiguration

    @Mock
    private lateinit var advancedConfiguration: AdvancedConfiguration

    @Mock
    private lateinit var discountParamsConfiguration: DiscountParamsConfiguration

    @Mock
    private lateinit var cardStatusRepository: CardStatusRepository

    private lateinit var initRequestBodyMapper: InitRequestBodyMapper

    @Before
    fun setUp() {

        initRequestBodyMapper = InitRequestBodyMapper(
            escManagerBehaviour,
            featureProvider,
            cardStatusRepository,
            trackingRepository)

        whenever(paymentSettingRepository.checkoutPreference)
            .thenReturn(StubCheckoutPreferenceUtils.stubPreferenceOneItemAndPayer())
        whenever(paymentSettingRepository.paymentConfiguration).thenReturn(paymentConfiguration)
        //whenever(paymentSettingRepository.paymentConfiguration.paymentProcessor).thenReturn(paymentProcessor)
        whenever(featureProvider.availableFeatures).thenReturn(
            CheckoutFeatures.Builder().build()
        )
        whenever(advancedConfiguration.discountParamsConfiguration).thenReturn(discountParamsConfiguration)
        whenever(paymentSettingRepository.advancedConfiguration).thenReturn(advancedConfiguration)
        whenever(paymentSettingRepository.publicKey).thenReturn("test_pk")
        whenever(paymentConfiguration.getCheckoutType()).thenReturn(CheckoutType.CUSTOM_REGULAR)
    }

    @Test
    fun mapFromPaymentSettingRepositoryShouldMapPublicKey() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        requestBody.publicKey.assertEquals(paymentSettingRepository.publicKey)
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithEmptyEscCardIdsShouldMapEmptyEscCardIds() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertTrue(requestBody.cardStatus.isEmpty())
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithEscCardIdsShouldMapEscCardIds() {
        val firstCardStatus = CardStatusDM("123", buildTokenData(CardStatusDM.TokenInfo.TokenStateDM.ENABLED), true)
        val secondCardStatus = CardStatusDM("456", buildTokenData(CardStatusDM.TokenInfo.TokenStateDM.IN_PROGRESS), true)
        whenever(cardStatusRepository.getCardsStatus()).thenReturn(listOf(firstCardStatus, secondCardStatus))
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        with(requestBody.cardStatus) {
            assertTrue(isNotEmpty())
            assertTrue(containsAll(listOf(firstCardStatus, secondCardStatus)))
        }
    }

    private fun buildTokenData(state: CardStatusDM.TokenInfo.TokenStateDM): CardStatusDM.TokenInfo {
        return CardStatusDM.TokenInfo(
            vProvisionedTokenId = UUID.randomUUID().toString(),
            updatedAt = Date(),
            state = state
        )
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithEmptyChargesShouldMapEmptyCharges() {
        whenever(paymentConfiguration.charges).thenReturn(arrayListOf())
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertTrue(requestBody.charges.isEmpty())
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithChargesShouldMapCharges() {
        val amChargeFree = PaymentTypeChargeRule.createChargeFreeRule(PaymentTypes.ACCOUNT_MONEY, "test msg")
        val debitCharge = PaymentTypeChargeRule.Builder(PaymentTypes.DEBIT_CARD, BigDecimal.TEN).build()
        whenever(paymentConfiguration.charges).thenReturn(arrayListOf(amChargeFree, debitCharge))
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        val expectedAMCharge = with(amChargeFree) {
            PaymentTypeChargeRuleDM(paymentTypeId, charge(), message, taxable)
        }
        val expectedDebitCharge = with(debitCharge) {
            PaymentTypeChargeRuleDM(paymentTypeId, charge(), null, taxable)
        }
        with(requestBody.charges) {
            size.assertEquals(2)
            assertTrue(containsAll(listOf(expectedAMCharge, expectedDebitCharge)))
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithNonZeroChargeCustomLabelAndTaxableFalseShouldMapChargeWithMessageAndTaxable() {
        val debitCharge = PaymentTypeChargeRule.Builder(PaymentTypes.DEBIT_CARD, BigDecimal.TEN)
            .setTaxable(false)
            .setLabel("Text custom")
            .build()
        whenever(paymentConfiguration.charges).thenReturn(arrayListOf(debitCharge))
        val expectedDebitCharge = with(debitCharge) {
            PaymentTypeChargeRuleDM(paymentTypeId, charge(), label, taxable)
        }
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertTrue(requestBody.charges.contains(expectedDebitCharge))
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithEmptyDiscountConfigurationShouldMapEmptyDiscountConfiguration() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        with(requestBody.discountParamsConfiguration) {
            assertNull(productId)
            assertTrue(labels.isEmpty())
            assertTrue(additionalParams.isEmpty())
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithDiscountConfigurationShouldMapDiscountConfiguration() {
        with(discountParamsConfiguration) {
            whenever(productId).thenReturn("1234")
            whenever(labels).thenReturn(setOf("123", "456"))
            whenever(additionalParams).thenReturn(mapOf(Pair("123","456")))
        }
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        with(requestBody.discountParamsConfiguration) {
            with(productId) {
                assertNotNull(this)
                this!!.assertEquals("1234")
            }
            with(labels) {
                size.assertEquals(2)
                assertTrue(containsAll(listOf("123", "456")))
            }
            with(additionalParams) {
                size.assertEquals(1)
                containsKey("123")
                getValue("123").assertEquals("456")
            }
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithCheckoutFeaturesShouldMapCheckoutFeatures() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        with(requestBody.features) {
            assertFalse(express)
            assertFalse(split)
            assertFalse(odrFlag)
            assertFalse(comboCard)
            assertFalse(hybridCard)
            assertFalse(pix)
            assertFalse(customTaxesCharges)
            assertFalse(cardsCustomTaxesCharges)
            assertFalse(taxableCharges)
            assertTrue(validationPrograms.isEmpty())
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithOpenPreferenceShouldMapOpenPreferenceAndEmptyCheckoutPreferenceId() {
        with(initRequestBodyMapper.map(paymentSettingRepository, null)) {
            assertNotNull(preference)
            assertNull(preferenceId)
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithPreferenceIdShouldMapEmptyOpenPreferenceAndPreferenceId() {
        whenever(paymentSettingRepository.checkoutPreference).thenReturn(null)
        whenever(paymentSettingRepository.checkoutPreferenceId).thenReturn("123456")
        with(initRequestBodyMapper.map(paymentSettingRepository, null)) {
            assertNotNull(preferenceId)
            preferenceId!!.assertEquals("123456")
            assertNull(preference)
        }
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithNullCardIdShouldMapNullCardId() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertNull(requestBody.newCardId)
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithCardIdShouldMapCardId() {
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, "card_id_test")
        assertNotNull(requestBody.newCardId)
        requestBody.newCardId!!.assertEquals("card_id_test")
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithNullFlowShouldMapNullFlow() {
        whenever(trackingRepository.flowId).thenReturn(null)
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertNull(requestBody.flow)
    }

    @Test
    fun mapFromPaymentSettingRepositoryWithFlowShouldMapFlow() {
        whenever(trackingRepository.flowId).thenReturn("flow_id")
        val requestBody = initRequestBodyMapper.map(paymentSettingRepository, null)
        assertNotNull(requestBody.flow)
        requestBody.flow!!.assertEquals("flow_id")
    }
}