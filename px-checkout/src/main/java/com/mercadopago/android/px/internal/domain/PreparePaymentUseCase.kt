package com.mercadopago.android.px.internal.domain

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository.*
import com.mercadopago.android.px.internal.extensions.ifSuccess
import com.mercadopago.android.px.internal.extensions.isNotNull
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.model.Discount
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.ResponseSectionStatus
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.PreparePaymentResponse
import com.mercadopago.android.px.tracking.internal.MPTracker

internal class PreparePaymentUseCase(
    private val preparePaymentRepository: PreparePaymentRepository,
    private val paymentDiscountRepository: PaymentDiscountRepository,
    private val discountRepository: DiscountRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Unit, PreparePaymentResponse>(tracker) {
    override suspend fun doExecute(param: Unit): Response<PreparePaymentResponse, MercadoPagoError> {
        // Clear last response
        paymentDiscountRepository.reset()
        return preparePaymentRepository.prepare().ifSuccess {
            handleDiscountsResponse(it)
        }
    }

    private fun handleDiscountsResponse(response: PreparePaymentResponse) = with(response) {
        val discountsStatus = status?.get(SECTION_DISCOUNTS)
        when (discountsStatus?.code) {
            ResponseSectionStatus.Code.OK,
            ResponseSectionStatus.Code.USE_FALLBACK -> configureDiscounts(paymentMethod!!)
            ResponseSectionStatus.Code.MISMATCH ->
                // Something happened with the discount that resulted in a mismatch with the one that we requested
                discountsStatus.message // This message has to be raised to the upper layer to show an recoverable error screen
            else -> Unit // Empty status for discounts means we don't have to do anything
        }
    }

    private fun configureDiscounts(paymentMethod: PaymentMethodDM) {
        val isSplit = paymentMethod.splitPaymentMethods?.isNotEmpty() == true
        paymentDiscountRepository.configure(
            PaymentDiscounts(
                primaryPaymentMethodDiscount = mergePaymentMethodDiscount(paymentMethod, isSplit, false),
                splitPaymentMethodDiscount = mergePaymentMethodDiscount(paymentMethod.splitPaymentMethods?.get(0), isSplit, true)
            )
        )
    }

    /**
     * Merges the PaymentMethodDM discount with the discount info. This info is taken from DiscountRepository when it's not a
     * split payment and from SplitConfiguration in AmountConfigurationRepository when it's a split payment.
     * In both cases we merge the token with this info so the object it's ready to be used without further processing.
     *
     * In future, prepare payment API should return this objects already mapped and ready to be used (to avoid having to merge it)
     *
     * @param paymentMethod The payment method data model to be mapped.
     * @param isSplit Represents if the payment is split.
     * @param isSplitPaymentMethod Represents if the received payment method is a split payment method.
     */
    private fun mergePaymentMethodDiscount(paymentMethod: PaymentMethodDM?, isSplit: Boolean, isSplitPaymentMethod: Boolean) =
        paymentMethod?.let {
            if (isSplit) {
                getSplitPaymentMethodDiscount(isSplitPaymentMethod)
            } else {
                getRegularPaymentMethodDiscount(PayerPaymentMethodKey(it.id, it.paymentTypeId))
            }.run {
                it.discountInfo?.token?.let { token ->
                    Discount.replaceWith(this, token)
                } ?: this // if discount info or token are null we use this as a fallback
            }
        }

    /**
     * Maps a payment method discount that is NOT for a split payment.
     */
    private fun getRegularPaymentMethodDiscount(payerPaymentMethodKey: PayerPaymentMethodKey): Discount? {
        val checkoutInitDiscount = discountRepository.getConfigurationFor(payerPaymentMethodKey).discount
        return runCatching {
            // We replace discount token with the correct one for this configuration (this is used as a fallback)
            Discount.replaceWith(checkoutInitDiscount, amountConfigurationRepository.getCurrentConfiguration().discountToken)
        }.getOrDefault(checkoutInitDiscount)
    }

    /**
     * Maps a payment method discount that is for a split payment
     */
    private fun getSplitPaymentMethodDiscount(isSplitPaymentMethod: Boolean): Discount? {
        val splitConfiguration = amountConfigurationRepository.getCurrentConfiguration().splitConfiguration
        checkNotNull(splitConfiguration)
        return if (isSplitPaymentMethod) {
            splitConfiguration.secondaryPaymentMethod.discount
        } else {
            splitConfiguration.primaryPaymentMethod.discount
        }
    }

    companion object {
        const val SECTION_DISCOUNTS = "discounts"
    }
}
