package com.mercadopago.android.px.internal.features.one_tap.confirm_button

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.mercadopago.android.px.core.data.network.ConnectionHelper
import com.mercadopago.android.px.internal.base.BaseState
import com.mercadopago.android.px.internal.base.BaseViewModelWithState
import com.mercadopago.android.px.internal.base.use_case.UseCase
import com.mercadopago.android.px.internal.features.pay_button.ConfirmButtonUiState
import com.mercadopago.android.px.internal.features.pay_button.UIError
import com.mercadopago.android.px.internal.features.pay_button.UIProgress
import com.mercadopago.android.px.internal.mappers.PayButtonViewModelMapper
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.exceptions.PreparePaymentMismatchError
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.ConfirmButtonPressedEvent
import java.lang.ref.WeakReference
import com.mercadopago.android.px.internal.viewmodel.PayButtonViewModel as ButtonConfig

internal abstract class ConfirmButtonViewModel<S : BaseState, H : ConfirmButton.Handler>(
    private val prepareProcessUseCase: UseCase<Unit, Unit>,
    private val connectionHelper: ConnectionHelper,
    customTextsRepository: CustomTextsRepository,
    payButtonViewModelMapper: PayButtonViewModelMapper,
    tracker: MPTracker
) : BaseViewModelWithState<S>(tracker), ConfirmButton.ViewModel {

    private val buttonTextMutableLiveData = MutableLiveData<ButtonConfig>()
    val buttonTextLiveData: LiveData<ButtonConfig>
        get() = buttonTextMutableLiveData
    protected val uiStateMutableLiveData = MediatorLiveData<ConfirmButtonUiState>()
    val uiStateLiveData: LiveData<ConfirmButtonUiState>
        get() = uiStateMutableLiveData

    protected var buttonConfig = payButtonViewModelMapper.map(customTextsRepository.customTexts)

    init {
        buttonTextMutableLiveData.value = buttonConfig
    }

    private lateinit var handlerReference: WeakReference<H>
    val handler: H
        get() = handlerReference.get()!!

    @CallSuper
    override fun onButtonPressed() {
        handler.getViewTrackPath(object : ConfirmButton.ViewTrackPathCallback {
            override fun call(viewTrackPath: String) {
                track(ConfirmButtonPressedEvent(viewTrackPath))
            }
        })
    }

    protected fun onPreProcess() {
        if (connectionHelper.hasConnection()) {
            handler.onPreProcess(object : ConfirmButton.OnReadyForProcessCallback {
                override fun call(paymentConfiguration: PaymentConfiguration) {
                    executePreProcess(paymentConfiguration)
                }
            })
        } else {
            manageNoConnection()
        }
    }

    protected fun onEnqueueProcess(paymentConfiguration: PaymentConfiguration) {
        handler.onEnqueueProcess(object : ConfirmButton.OnEnqueueResolvedCallback {
            override fun success() {
                prepareProcessUseCase.runCatching {
                    execute(
                        Unit,
                        success = { executeProcess(paymentConfiguration) },
                        failure = { handlePrepareProcessFailure(it, paymentConfiguration) }
                    )
                }.onFailure { uiStateMutableLiveData.value = UIError.NotRecoverableError(it) }
            }

            override fun failure(error: MercadoPagoError) {
                uiStateMutableLiveData.value = UIProgress.ButtonLoadingCanceled
                handler.onProcessError(error)
            }
        })
    }

    protected abstract fun manageNoConnection()

    protected abstract fun executePreProcess(paymentConfiguration: PaymentConfiguration)

    protected abstract fun executeProcess(paymentConfiguration: PaymentConfiguration)

    private fun handlePrepareProcessFailure(error: MercadoPagoError, paymentConfiguration: PaymentConfiguration) {
        if (error is PreparePaymentMismatchError) {
            // If it's a mismatch error then we need to reload the checkout so we launch a recoverable error
            uiStateMutableLiveData.value = UIError.RecoverableError(error)
        } else {
            // If it's another error then we continue the process, since it's not part of the critical path
            executeProcess(paymentConfiguration)
        }
    }

    fun attach(handler: H) {
        handlerReference = WeakReference(handler)
    }

    fun detach() {
        handlerReference.clear()
    }
}
