package com.mercadopago.android.px.internal.domain

import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.callbacks.Response
import com.mercadopago.android.px.internal.callbacks.next
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository
import com.mercadopago.android.px.internal.datasource.PaymentDiscountRepository.PaymentDiscounts
import com.mercadopago.android.px.internal.extensions.ifFailure
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository
import com.mercadopago.android.px.internal.repository.DiscountRepository
import com.mercadopago.android.px.internal.repository.PreparePaymentRepository
import com.mercadopago.android.px.internal.repository.UserSelectionRepository
import com.mercadopago.android.px.model.Discount
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.exceptions.PreparePaymentMismatchError
import com.mercadopago.android.px.model.internal.ResponseSectionStatus
import com.mercadopago.android.px.model.internal.payment_prepare.PaymentMethodDM
import com.mercadopago.android.px.model.internal.payment_prepare.getDiscountStatus
import com.mercadopago.android.px.tracking.internal.MPTracker

internal class PreparePaymentUseCase(
    private val preparePaymentRepository: PreparePaymentRepository,
    private val paymentDiscountRepository: PaymentDiscountRepository,
    private val discountRepository: DiscountRepository,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val userSelectionRepository: UserSelectionRepository,
    tracker: MPTracker,
    override val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) : UseCase<Unit, Unit>(tracker) {
    override suspend fun doExecute(param: Unit): Response<Unit, MercadoPagoError> {
        paymentDiscountRepository.reset()
        return preparePaymentRepository.prepare()
            .ifFailure {
                configureDiscounts(null)
            }
            .next {
                val discountStatus = it.getDiscountStatus()
                when (discountStatus?.code) {
                    ResponseSectionStatus.Code.OK,
                    ResponseSectionStatus.Code.USE_FALLBACK -> {
                        configureDiscounts(it.paymentMethod)
                        Response.Success(Unit)
                    }
                    ResponseSectionStatus.Code.MISMATCH ->
                        Response.Failure(PreparePaymentMismatchError(discountStatus.message))
                    else -> Response.Success(Unit)
                }
            }
    }

    private fun configureDiscounts(paymentMethod: PaymentMethodDM?) {
        val isSplit = userSelectionRepository.secondaryPaymentMethod != null
        paymentDiscountRepository.configure(
            PaymentDiscounts(
                mergeDiscount(paymentMethod?.discountInfo?.token, isSplit, false),
                mergeDiscount(paymentMethod?.splitPaymentMethods?.get(0)?.discountInfo?.token, isSplit, true)
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
     * @param token The discount token from the payment method to be merged.
     * @param isSplit Represents if the payment is split.
     * @param isSplitPaymentMethod Represents if the received payment method is a split payment method.
     */
    private fun mergeDiscount(token: String?, isSplit: Boolean, isSplitPaymentMethod: Boolean) =
        when {
            !isSplit && !isSplitPaymentMethod -> getRegularPaymentMethodDiscount()
            isSplit -> getSplitPaymentMethodDiscount(isSplitPaymentMethod)
            else -> null
        }.run {
            token?.let {
                Discount.replaceWith(this, it)
            } ?: this // if discount info or token are null we use this as a fallback
        }

    /**
     * Gets a payment method discount that is NOT for a split payment
     */
    private fun getRegularPaymentMethodDiscount(): Discount? {
        val checkoutInitDiscount = discountRepository.getCurrentConfiguration().discount
        return runCatching {
            // We replace discount token with the correct one for this configuration (this is used as a fallback)
            Discount.replaceWith(checkoutInitDiscount, amountConfigurationRepository.getCurrentConfiguration().discountToken)
        }.getOrDefault(checkoutInitDiscount)
    }

    /**
     * Gets a payment method discount that is for a split payment
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
}
