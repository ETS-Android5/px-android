package com.mercadopago.android.px.internal.viewmodel.drawables

import com.mercadopago.android.px.internal.datasource.CustomOptionIdSolver
import com.mercadopago.android.px.internal.features.generic_modal.ActionType
import com.mercadopago.android.px.internal.features.generic_modal.FromModalToGenericDialogItem
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialogItem
import com.mercadopago.android.px.internal.mappers.CardDrawerCustomViewModelMapper
import com.mercadopago.android.px.internal.mappers.CardUiMapper
import com.mercadopago.android.px.internal.mappers.NonNullMapper
import com.mercadopago.android.px.internal.repository.*
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem.Parameters
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.model.internal.Application
import com.mercadopago.android.px.model.internal.OneTapItem
import com.mercadopago.android.px.model.one_tap.CheckoutBehaviour
import com.mercadopago.android.px.internal.repository.PayerPaymentMethodKey as Key
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentCommons.ByApplication
as CommonsByApplication

internal class PaymentMethodDrawableItemMapper(
    private val chargeRepository: ChargeRepository,
    private val disabledPaymentMethodRepository: DisabledPaymentMethodRepository,
    private val applicationSelectedRepository: ApplicationSelectionRepository,
    private val cardUiMapper: CardUiMapper,
    private val cardDrawerCustomViewModelMapper: CardDrawerCustomViewModelMapper,
    private val payerPaymentMethodRepository: PayerPaymentMethodRepository,
    private val modalRepository: ModalRepository
) : NonNullMapper<OneTapItem, DrawableFragmentItem?>() {

    override fun map(value: OneTapItem): DrawableFragmentItem? {
        val genericDialogItem = value.getBehaviour(CheckoutBehaviour.Type.TAP_CARD)?.modal?.let { modal ->
            modalRepository.value[modal]?.let {
                FromModalToGenericDialogItem(ActionType.DISMISS, modal).map(it)
            }
        }
        val parameters = getParameters(value, payerPaymentMethodRepository.value, genericDialogItem)
        with(value) {
            return when {
                isCard || isAccountMoney -> DrawableFragmentItem(parameters)
                isConsumerCredits -> ConsumerCreditsDrawableFragmentItem(parameters, consumerCredits)
                isNewCard || isOfflineMethods -> OtherPaymentMethodFragmentItem(parameters, newCard, offlineMethods)
                else -> null
            }
        }
    }


    private fun getCardDrawable(
        accountMoneyMetadata: AccountMoneyMetadata?,
        cardMetadata: CardMetadata?,
        paymentMethod: Application.PaymentMethod): CardDrawable? {

        return when {
            PaymentTypes.isAccountMoney(paymentMethod.type) -> accountMoneyMetadata?.displayInfo?.let(cardUiMapper::map)
            PaymentTypes.isCardPaymentType(paymentMethod.type) -> cardMetadata?.displayInfo?.let(cardUiMapper::map)
            else -> null
        }?.let { CardDrawable(paymentMethod.id, it) }
    }

    private fun getParameters(
        oneTapItem: OneTapItem,
        customSearchItems: List<CustomSearchItem>,
        genericDialogItem: GenericDialogItem?
    ): Parameters {
        val displayInfo = oneTapItem.displayInfo

        val defaultCustomOptionId = CustomOptionIdSolver.defaultCustomOptionId(oneTapItem)

        val paymentMethodType = applicationSelectedRepository[defaultCustomOptionId].paymentMethod.type
        val commonsByApplication = CommonsByApplication(paymentMethodType).also {
            oneTapItem.getApplications().forEach { application ->
                val customOptionIdByApplication = CustomOptionIdSolver.getByApplication(oneTapItem, application)
                val (description, issuerName) = customSearchItems.firstOrNull { c ->
                    c.id == customOptionIdByApplication
                }?.let {
                    Pair(it.description.orEmpty(), it.issuer?.name.orEmpty())
                } ?: Pair(TextUtil.EMPTY, TextUtil.EMPTY)

                val paymentTypeId = application.paymentMethod.type
                it[application] = DrawableFragmentCommons(
                    customOptionIdByApplication,
                    application.status,
                    chargeRepository.getChargeRule(paymentTypeId)?.message,
                    disabledPaymentMethodRepository[Key(customOptionIdByApplication, paymentTypeId)],
                    description,
                    issuerName,
                    getCardDrawable(oneTapItem.accountMoney, oneTapItem.card, application.paymentMethod)
                )
            }
        }

        return Parameters(
            commonsByApplication,
            displayInfo?.bottomDescription,
            oneTapItem.benefits?.reimbursement,
            genericDialogItem,
            cardDrawerCustomViewModelMapper.mapToSwitchModel(displayInfo?.cardDrawerSwitch, paymentMethodType)
        )
    }
}
