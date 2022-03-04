package com.mercadopago.android.px.internal.mappers

import com.mercadopago.android.px.internal.datasource.CustomOptionIdSolver
import com.mercadopago.android.px.internal.features.AmountDescriptorViewModelFactory
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.AmountRepository
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository
import com.mercadopago.android.px.internal.repository.ChargeRepository
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.util.isHighlightCharge
import com.mercadopago.android.px.internal.view.AmountDescriptorView
import com.mercadopago.android.px.internal.view.ElementDescriptorView
import com.mercadopago.android.px.internal.view.SummaryDetailDescriptorMapper
import com.mercadopago.android.px.internal.view.SummaryView
import com.mercadopago.android.px.internal.viewmodel.SummaryModel
import com.mercadopago.android.px.model.AmountConfiguration
import com.mercadopago.android.px.model.DiscountConfigurationModel
import com.mercadopago.android.px.model.PayerCost
import com.mercadopago.android.px.model.commission.PaymentTypeChargeRule
import com.mercadopago.android.px.model.internal.OneTapItem

internal class SummaryViewModelMapper(
    private val discountRepository: DiscountRepository,
    private val amountRepository: AmountRepository,
    private val elementDescriptorViewModel: ElementDescriptorView.Model,
    private val onClickListener: AmountDescriptorView.OnClickListener,
    private val chargeRepository: ChargeRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val customTextsRepository: CustomTextsRepository,
    private val summaryDetailDescriptorMapper: SummaryDetailDescriptorMapper,
    private val applicationSelectionRepository: ApplicationSelectionRepository,
    private val amountDescriptorViewModelFactory: AmountDescriptorViewModelFactory
) : Mapper<OneTapItem, SummaryModel>() {

    private val cache = mutableMapOf<Key, SummaryView.Model>()

    override fun map(values: Iterable<OneTapItem>) = mutableListOf<SummaryModel>().also {
        values.forEach { value ->
            val currentPmTypeSelection = getCurrentPmTypeSelection(value)
            it.add(SummaryModel(currentPmTypeSelection, mapToSummaryViewModel(value)))
        }
    }

    override fun map(value: OneTapItem) = SummaryModel(
        value.getDefaultPaymentMethodType(),
        mapToSummaryViewModel(value))

    private fun mapToSummaryViewModel(value: OneTapItem): Map<String, SummaryView.Model> {
        val map = mutableMapOf<String, SummaryView.Model>()
        value.getApplications().forEach { application ->
            val customOptionId = CustomOptionIdSolver.getByApplication(value, application)
            val paymentMethodTypeId = application.paymentMethod.type
            map[paymentMethodTypeId] = mapWithCache(customOptionId, paymentMethodTypeId)
        }

        return map
    }

    private fun mapWithCache(customOptionId: String, paymentMethodTypeId: String): SummaryView.Model {
        val key = getKey(customOptionId, paymentMethodTypeId)
        return if (cache.containsKey(key)) {
            cache[key]!!
        } else {
            createModel(paymentMethodTypeId,
                getDiscountConfiguration(customOptionId, paymentMethodTypeId),
                getAmountConfiguration(customOptionId, paymentMethodTypeId))
                .also { cache[key] = it }
        }
    }

    private fun getCurrentPmTypeSelection(oneTapItem: OneTapItem): String {
        return applicationSelectionRepository[oneTapItem].paymentMethod.type
    }

    private fun createModel(
        paymentTypeId: String,
        discountModel: DiscountConfigurationModel,
        amountConfiguration: AmountConfiguration?): SummaryView.Model {
        val chargeRule = chargeRepository.getChargeRule(paymentTypeId)
        val summaryDetailList = summaryDetailDescriptorMapper.map(
            SummaryDetailDescriptorMapper.Model(discountModel, chargeRule, amountConfiguration, onClickListener))

        val totalRow = amountDescriptorViewModelFactory.create(
            customTextsRepository, amountRepository.getAmountToPay(paymentTypeId, discountModel)
        )
        return SummaryView.Model(elementDescriptorViewModel, summaryDetailList, totalRow)
    }

    fun updateTotalValue(
        customOptionId: String,
        paymentMethodTypeId: String,
        paymentCost: PayerCost
    ): SummaryView.Model {
        val totalUpdated = amountDescriptorViewModelFactory.create(
            customTextsRepository, paymentCost.totalAmount)
        val chargeRule = chargeRepository.getChargeRule(paymentMethodTypeId)
        val discountModel = getDiscountConfiguration(customOptionId,paymentMethodTypeId)
        val amountConfiguration = getAmountConfiguration(customOptionId, paymentMethodTypeId)
        val summaryDetailList = summaryDetailDescriptorMapper.map(
            SummaryDetailDescriptorMapper.Model(discountModel, chargeRule, amountConfiguration, onClickListener)
        )
        return SummaryView.Model(elementDescriptorViewModel,summaryDetailList, totalUpdated)
    }

    private fun getDiscountConfiguration(
        customOptionId: String,
        paymentMethodTypeId: String): DiscountConfigurationModel {
        return discountRepository.getConfigurationFor(
            PayerPaymentMethodKey(customOptionId, paymentMethodTypeId))
    }

    private fun getAmountConfiguration(
        customOptionId: String,
        paymentMethodTypeId: String): AmountConfiguration? {
        return amountConfigurationRepository.getConfigurationFor(
            PayerPaymentMethodKey(customOptionId, paymentMethodTypeId))
    }

    private fun getKey(customOptionId: String, paymentMethodTypeId: String): Key {
        val chargeRule = chargeRepository.getChargeRule(paymentMethodTypeId)
        val amountConfiguration: AmountConfiguration? = getAmountConfiguration(customOptionId, paymentMethodTypeId)
        val hasSplit = amountConfiguration != null && amountConfiguration.allowSplit()

        return Key(getDiscountConfiguration(customOptionId, paymentMethodTypeId), chargeRule, hasSplit)
    }

    internal class Key(
        discountConfigurationModel: DiscountConfigurationModel,
        paymentTypeChargeRule: PaymentTypeChargeRule?,
        hasSplit: Boolean) {
        private val discountConfigurationModel: DiscountConfigurationModel?
        private val paymentTypeChargeRule: PaymentTypeChargeRule?
        private val hasSplit: Boolean?
        override fun hashCode(): Int {
            return (discountConfigurationModel?.hashCode() ?: 0) xor
                (paymentTypeChargeRule?.hashCode() ?: 0) xor
                (hasSplit?.hashCode() ?: 0)
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Key) {
                return false
            }
            return (other.discountConfigurationModel == discountConfigurationModel
                && other.paymentTypeChargeRule == paymentTypeChargeRule
                && other.hasSplit == hasSplit)
        }

        init {
            this.discountConfigurationModel = discountConfigurationModel
            this.paymentTypeChargeRule = if (paymentTypeChargeRule != null && paymentTypeChargeRule.isHighlightCharge())
                null else paymentTypeChargeRule
            this.hasSplit = hasSplit
        }
    }
}