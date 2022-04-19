package com.mercadopago.android.px.internal.features.one_tap.confirm_button

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mercadopago.android.px.core.data.network.ConnectionHelper
import com.mercadopago.android.px.core.v2.PaymentMethodsData
import com.mercadopago.android.px.internal.base.BaseState
import com.mercadopago.android.px.internal.base.use_case.UserSelectionUseCase
import com.mercadopago.android.px.internal.datasource.PaymentDataFactory
import com.mercadopago.android.px.internal.domain.PreparePaymentUseCase
import com.mercadopago.android.px.internal.features.pay_button.UIError
import com.mercadopago.android.px.internal.features.pay_button.UIProgress
import com.mercadopago.android.px.internal.features.validation_program.ValidationProgramUseCase
import com.mercadopago.android.px.internal.mappers.PayButtonViewModelMapper
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.NoConnectionFrictionTracker
import kotlinx.android.parcel.Parcelize

private const val PROCESS_TIME_OUT = 10000

internal class SelectorConfirmButtonViewModel(
    private val paymentSettingRepository: PaymentSettingRepository,
    preparePaymentUseCase: PreparePaymentUseCase,
    private val userSelectionUseCase: UserSelectionUseCase,
    private val validationProgramUseCase: ValidationProgramUseCase,
    private val paymentDataFactory: PaymentDataFactory,
    connectionHelper: ConnectionHelper,
    customTextsRepository: CustomTextsRepository,
    payButtonViewModelMapper: PayButtonViewModelMapper,
    tracker: MPTracker
) : ConfirmButtonViewModel<SelectorConfirmButtonViewModel.State, ConfirmButton.Handler>(
    preparePaymentUseCase,
    connectionHelper,
    customTextsRepository,
    payButtonViewModelMapper,
    tracker
) {

    private val processFinishedMutableLiveData = MutableLiveData<PaymentMethodsData>()
    val processFinished: LiveData<PaymentMethodsData>
        get() = processFinishedMutableLiveData

    override fun onButtonPressed() {
        super.onButtonPressed()
        onPreProcess()
    }

    override fun executePreProcess(paymentConfiguration: PaymentConfiguration) {
        state.paymentConfiguration = paymentConfiguration
        uiStateMutableLiveData.value = UIProgress.ButtonLoadingStarted(PROCESS_TIME_OUT, buttonConfig)
        userSelectionUseCase.execute(
            paymentConfiguration,
            success = { onEnqueueProcess(paymentConfiguration) },
            failure = {
                uiStateMutableLiveData.value = UIProgress.ButtonLoadingCanceled
                handler.onProcessError(it)
            }
        )
    }

    override fun executeProcess(paymentConfiguration: PaymentConfiguration) {
        val paymentDataList = paymentDataFactory.create()
        validationProgramUseCase.execute(paymentDataList, success = { validationProgramId ->
            val securityType = paymentSettingRepository.securityType.value
            val checkoutPreference = paymentSettingRepository.checkoutPreference

            if (checkoutPreference != null) {
                state.checkoutData = PaymentMethodsData(
                    paymentDataList,
                    checkoutPreference,
                    securityType,
                    validationProgramId
                )
                uiStateMutableLiveData.value = UIProgress.ButtonLoadingFinished()
            } else {
                uiStateMutableLiveData.value = UIProgress.ButtonLoadingCanceled
                handler.onProcessError(MercadoPagoError.createNotRecoverable("checkoutPreference should not be null"))
            }
        }, failure = {
            uiStateMutableLiveData.value = UIProgress.ButtonLoadingCanceled
            handler.onProcessError(it)
        })

        handler.onProcessExecuted(paymentConfiguration)
    }

    override fun onAnimationFinished() {
        handler.onProcessFinished(object : ConfirmButton.OnPaymentFinishedCallback {
            override fun call() {
                processFinishedMutableLiveData.value = state.checkoutData
            }
        })
    }

    override fun manageNoConnection() {
        track(NoConnectionFrictionTracker)
        uiStateMutableLiveData.value = UIError.ConnectionError(++state.retryCounter)
    }

    override fun initState() = State()

    @Parcelize
    data class State(
        var paymentConfiguration: PaymentConfiguration? = null,
        var checkoutData: PaymentMethodsData? = null,
        var retryCounter: Int = 0
    ) : BaseState

    override fun onStateRestored() {
        if (state.checkoutData != null) {
            uiStateMutableLiveData.value = UIProgress.ButtonLoadingFinished()
        } else if (state.paymentConfiguration != null) {
            onPreProcess()
        }
    }
}
