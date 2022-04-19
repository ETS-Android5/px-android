package com.mercadopago.android.px.internal.features.payment_result.remedies

import androidx.lifecycle.MutableLiveData
import com.mercadopago.android.px.addons.ESCManagerBehaviour
import com.mercadopago.android.px.internal.base.BaseState
import com.mercadopago.android.px.internal.base.BaseViewModelWithState
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithPaymentRecoveryUseCase
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithPaymentRecoveryParams
import com.mercadopago.android.px.internal.datasource.mapper.FromPayerPaymentMethodToCardMapper
import com.mercadopago.android.px.internal.features.one_tap.confirm_button.ConfirmButton
import com.mercadopago.android.px.internal.features.payment_result.presentation.PaymentResultButton
import com.mercadopago.android.px.internal.repository.*
import com.mercadopago.android.px.internal.util.TokenCreationWrapper
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.Card
import com.mercadopago.android.px.model.PayerCost
import com.mercadopago.android.px.model.PaymentData
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.model.internal.remedies.RemedyPaymentMethod
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.ChangePaymentMethodEvent
import com.mercadopago.android.px.tracking.internal.events.RemedyEvent
import com.mercadopago.android.px.tracking.internal.events.RemedyModalAbortEvent
import com.mercadopago.android.px.tracking.internal.events.RemedyModalView
import com.mercadopago.android.px.tracking.internal.model.RemedyTrackData
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class RemediesViewModel(
    private val paymentRepository: PaymentRepository,
    private val paymentSettingRepository: PaymentSettingRepository,
    private val cardTokenRepository: CardTokenRepository,
    private val escManagerBehaviour: ESCManagerBehaviour,
    private val amountConfigurationRepository: AmountConfigurationRepository,
    private val applicationSelectionRepository: ApplicationSelectionRepository,
    private val tokenizeWithPaymentRecoveryUseCase: TokenizeWithPaymentRecoveryUseCase,
    private val oneTapItemRepository: OneTapItemRepository,
    private val fromPayerPaymentMethodToCardMapper: FromPayerPaymentMethodToCardMapper,
    tracker: MPTracker
) : BaseViewModelWithState<RemediesViewModel.State>(tracker), Remedies.ViewModel {

    val remedyState: MutableLiveData<RemedyState> = MutableLiveData()
    private var isSilverBullet: Boolean = false
    private var paymentConfiguration: PaymentConfiguration? = null
    private var card: Card? = null
    private var showedModal = false
    private lateinit var remediesModel: RemediesModel
    private lateinit var previousPaymentModel: PaymentModel

    fun init(remediesModel: RemediesModel, previousPaymentModel: PaymentModel) {
        this.remediesModel = remediesModel
        this.previousPaymentModel = previousPaymentModel

        isSilverBullet = remediesModel.retryPayment?.isAnotherMethod == true
        val methodIds = getMethodIds()
        val customOptionId = methodIds.customOptionId
        configureRetryPayment(customOptionId)
        configureHighRisk()
        card = fromPayerPaymentMethodToCardMapper.map(PayerPaymentMethodKey(customOptionId, methodIds.typeId))
        paymentConfiguration = PaymentConfiguration(
            methodIds.methodId, methodIds.typeId, customOptionId,
            card?.isSecurityCodeRequired() == true, false, getPayerCost(customOptionId)
        )
    }

    private fun configureRetryPayment(customOptionId: String) {
        val oneTapItem = oneTapItemRepository[customOptionId]
        remediesModel.retryPayment?.let {
            if (isSilverBullet) {
                val paymentTypeId =
                    previousPaymentModel.remedies.suggestedPaymentMethod?.alternativePaymentMethod?.paymentTypeId
                applicationSelectionRepository[oneTapItem] = oneTapItem.getApplications().first { application ->
                    application.paymentMethod.type == paymentTypeId
                }
            }
            remedyState.value = RemedyState.ShowRetryPaymentRemedy(Pair(it, oneTapItem))
        }
    }

    private fun configureHighRisk() {
        remediesModel.highRisk?.let {
            remedyState.value = RemedyState.ShowKyCRemedy(it)
        }
    }

    override fun onPayButtonPressed(callback: ConfirmButton.OnEnqueueResolvedCallback) {
        if (isSilverBullet) {
            startPayment(callback)
        } else {
            startCvvRecovery(callback)
        }
    }

    override fun onPrePayment(callback: ConfirmButton.OnReadyForProcessCallback) {
        previousPaymentModel.remedies.suggestedPaymentMethod?.modal?.takeUnless {
            showedModal
        }?.let {
            track(RemedyModalView())
            remedyState.value = RemedyState.ShowModal(it)
        } ?: callback.call(paymentConfiguration!!)
    }

    private fun getMethodIds(): MethodIds {
        return previousPaymentModel.run {
            if (isSilverBullet) {
                remedies.suggestedPaymentMethod!!.alternativePaymentMethod.let {
                    MethodIds.with(it)
                }
            } else {
                MethodIds.with(paymentResult.paymentData)
            }
        }
    }

    private fun getPayerCost(customOptionId: String): PayerCost? {
        return previousPaymentModel.run {
            if (isSilverBullet) {
                remedies.suggestedPaymentMethod?.alternativePaymentMethod?.installmentsList?.run {
                    if (isNotEmpty()) {
                        get(0).let {
                            amountConfigurationRepository.getConfigurationSelectedFor(customOptionId)?.run {
                                for (i in 0 until payerCosts.size) {
                                    val payerCost = payerCosts[i]
                                    if (payerCost.installments == it.installments) {
                                        remediesModel.retryPayment?.payerCost = RemediesPayerCost(i, it.installments)
                                        return payerCost
                                    }
                                }
                            }
                        }
                    }
                    return null
                }
            } else {
                paymentResult.paymentData.payerCost
            }
        }
    }

    private fun startPayment(callback: ConfirmButton.OnEnqueueResolvedCallback) {
        track(RemedyEvent(getRemedyTrackData(RemedyType.PAYMENT_METHOD_SUGGESTION), showedModal))
        remediesModel.retryPayment?.cvvModel?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val response = TokenCreationWrapper.Builder(cardTokenRepository, escManagerBehaviour)
                    .with(card!!).build().createTokenWithCvv(state.cvv)

                withContext(Dispatchers.Main) {
                    response.resolve(success = { token ->
                        paymentSettingRepository.configure(token)
                        callback.success()
                    }, error = { callback.failure(it) })
                }
            }
        } ?: callback.success()
    }

    private fun startCvvRecovery(callback: ConfirmButton.OnEnqueueResolvedCallback) {
        track(RemedyEvent(getRemedyTrackData(RemedyType.CVV_REQUEST), showedModal))
        tokenizeWithPaymentRecoveryUseCase.execute(
            TokenizeWithPaymentRecoveryParams(state.paymentRecovery, state.cvv),
            success = { callback.success() },
            failure = { callback.failure(it) })
    }

    override fun onButtonPressed(action: PaymentResultButton.Action) {
        when (action) {
            PaymentResultButton.Action.CHANGE_PM, PaymentResultButton.Action.MODAL_CHANGE_PM -> {
                track(ChangePaymentMethodEvent(action == PaymentResultButton.Action.MODAL_CHANGE_PM))
                remedyState.value = RemedyState.ChangePaymentMethod
            }
            PaymentResultButton.Action.KYC -> remediesModel.highRisk?.let {
                track(RemedyEvent(getRemedyTrackData(RemedyType.KYC_REQUEST), showedModal))
                remedyState.value = RemedyState.GoToKyc(it.deepLink)
            }
            PaymentResultButton.Action.MODAL_PAY -> {
                showedModal = true
                remedyState.value = RemedyState.Pay
            }

            else -> TODO()
        }
    }

    override fun onCvvFilled(cvv: String) {
        state.cvv = cvv
    }

    override fun initState() = State(paymentRepository.createPaymentRecovery())

    private fun getRemedyTrackData(type: RemedyType) = previousPaymentModel.payment!!.let {
        RemedyTrackData(type.getType(), remediesModel.trackingData, it.paymentStatus, it.paymentStatusDetail)
    }

    fun trackRemedyModalAbort() {
        track(RemedyModalAbortEvent())
    }

    private data class MethodIds(val methodId: String, val typeId: String, val customOptionId: String) {
        companion object {
            fun with(paymentData: PaymentData): MethodIds {
                return paymentData.run {
                    val methodId = paymentMethod.id
                    MethodIds(methodId, paymentMethod.paymentTypeId, token?.cardId ?: methodId)
                }
            }

            fun with(remedyPaymentMethod: RemedyPaymentMethod) =
                MethodIds(
                    remedyPaymentMethod.paymentMethodId, remedyPaymentMethod.paymentTypeId,
                    remedyPaymentMethod.customOptionId
                )
        }
    }

    @Parcelize
    data class State(var paymentRecovery: PaymentRecovery, var cvv: String = "") : BaseState
}
