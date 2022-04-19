package com.mercadopago.android.px.internal.features.pay_button

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations.map
import com.mercadopago.android.px.addons.model.SecurityValidationData
import com.mercadopago.android.px.core.data.network.ConnectionHelper
import com.mercadopago.android.px.internal.audio.AudioPlayer
import com.mercadopago.android.px.internal.audio.SelectPaymentSoundUseCase
import com.mercadopago.android.px.internal.base.BaseState
import com.mercadopago.android.px.internal.base.use_case.UserSelectionUseCase
import com.mercadopago.android.px.internal.callbacks.PaymentServiceEventHandler
import com.mercadopago.android.px.internal.core.ProductIdProvider
import com.mercadopago.android.px.internal.datasource.PaymentDataFactory
import com.mercadopago.android.px.internal.domain.PreparePaymentUseCase
import com.mercadopago.android.px.internal.extensions.isNotNullNorEmpty
import com.mercadopago.android.px.internal.features.PaymentResultViewModelFactory
import com.mercadopago.android.px.internal.features.checkout.PostPaymentUrlsMapper
import com.mercadopago.android.px.internal.features.explode.ExplodeDecoratorMapper
import com.mercadopago.android.px.internal.features.one_tap.confirm_button.ConfirmButton
import com.mercadopago.android.px.internal.features.one_tap.confirm_button.ConfirmButtonViewModel
import com.mercadopago.android.px.internal.features.pay_button.UIProgress.ButtonLoadingCanceled
import com.mercadopago.android.px.internal.features.pay_button.UIProgress.ButtonLoadingFinished
import com.mercadopago.android.px.internal.features.pay_button.UIProgress.ButtonLoadingStarted
import com.mercadopago.android.px.internal.features.pay_button.UIProgress.FingerprintRequired
import com.mercadopago.android.px.internal.features.pay_button.UIResult.VisualProcessorResult
import com.mercadopago.android.px.internal.features.payment_congrats.CongratsResult
import com.mercadopago.android.px.internal.features.payment_congrats.CongratsResultFactory
import com.mercadopago.android.px.internal.livedata.MediatorSingleLiveData
import com.mercadopago.android.px.internal.mappers.PayButtonViewModelMapper
import com.mercadopago.android.px.internal.model.SecurityType
import com.mercadopago.android.px.internal.repository.CustomTextsRepository
import com.mercadopago.android.px.internal.repository.PaymentRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.util.SecurityValidationDataFactory
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction
import com.mercadopago.android.px.model.Currency
import com.mercadopago.android.px.model.IParcelablePaymentDescriptor
import com.mercadopago.android.px.model.Payment
import com.mercadopago.android.px.model.PaymentRecovery
import com.mercadopago.android.px.model.PaymentResult
import com.mercadopago.android.px.model.exceptions.MercadoPagoError
import com.mercadopago.android.px.model.internal.PaymentConfiguration
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.BiometricsFrictionTracker
import com.mercadopago.android.px.tracking.internal.events.FrictionEventTracker
import com.mercadopago.android.px.tracking.internal.events.NoConnectionFrictionTracker
import com.mercadopago.android.px.tracking.internal.views.OneTapViewTracker
import kotlinx.android.parcel.Parcelize

internal class PayButtonViewModel(
    private val congratsResultFactory: CongratsResultFactory,
    private val paymentService: PaymentRepository,
    private val productIdProvider: ProductIdProvider,
    connectionHelper: ConnectionHelper,
    private val paymentSettingRepository: PaymentSettingRepository,
    customTextsRepository: CustomTextsRepository,
    payButtonViewModelMapper: PayButtonViewModelMapper,
    private val postPaymentUrlsMapper: PostPaymentUrlsMapper,
    preparePaymentUseCase: PreparePaymentUseCase,
    private val selectPaymentSoundUseCase: SelectPaymentSoundUseCase,
    private val userSelectionUseCase: UserSelectionUseCase,
    private val factory: PaymentResultViewModelFactory,
    private val paymentDataFactory: PaymentDataFactory,
    private val audioPlayer: AudioPlayer,
    tracker: MPTracker
) : ConfirmButtonViewModel<PayButtonViewModel.State, PayButton.Handler>(
    preparePaymentUseCase,
    connectionHelper,
    customTextsRepository,
    payButtonViewModelMapper,
    tracker
), PayButton.ViewModel {

    private val postPaymentMutableLiveData = MediatorLiveData<PostPaymentFlowStarted>()
    val postPaymentLiveData: LiveData<PostPaymentFlowStarted>
        get() = postPaymentMutableLiveData

    val congratsResultLiveData = MediatorSingleLiveData<CongratsResult>()

    private fun <X : Any, I> transform(liveData: LiveData<X>, block: (content: X) -> I): LiveData<I?> {
        return map(liveData) {
            state.observingService = false
            block(it)
        }
    }

    override fun onButtonPressed() {
        super.onButtonPressed()
        preparePayment()
    }

    override fun preparePayment() {
        state.paymentConfiguration = null
        onPreProcess()
    }

    private fun startAuthentication(paymentConfiguration: PaymentConfiguration) {
        state.paymentConfiguration = paymentConfiguration
        val data: SecurityValidationData = SecurityValidationDataFactory
            .create(productIdProvider, paymentSettingRepository.checkoutPreference!!.totalAmount, paymentConfiguration)
        uiStateMutableLiveData.value = FingerprintRequired(data)
    }

    override fun handleAuthenticationResult(isSuccess: Boolean, securityRequested: Boolean) {
        if (isSuccess) {
            paymentSettingRepository.configure(if (securityRequested) SecurityType.SECOND_FACTOR else SecurityType.NONE)
            startPayment()
        } else {
            track(BiometricsFrictionTracker)
        }
    }

    override fun startPayment() {
        runCatching {
            val configuration = state.paymentConfiguration
            checkNotNull(configuration) { "No payment configuration provided" }
            if (paymentService.isExplodingAnimationCompatible) {
                uiStateMutableLiveData.value = ButtonLoadingStarted(paymentService.paymentTimeout, buttonConfig)
            }

            userSelectionUseCase
                .execute(configuration,
                    success = { onEnqueueProcess(configuration) },
                    failure = {
                        handler.onProcessError(it)
                        uiStateMutableLiveData.value = ButtonLoadingCanceled
                    }
                )
        }.onFailure { uiStateMutableLiveData.value = UIError.NotRecoverableError(it) }
    }

    override fun executePreProcess(paymentConfiguration: PaymentConfiguration) {
        if (paymentConfiguration.customOptionId.isNotNullNorEmpty()) {
            paymentSettingRepository.clearToken()
        }
        startAuthentication(paymentConfiguration)
    }

    override fun executeProcess(paymentConfiguration: PaymentConfiguration) {
        with(paymentService) {
            startExpressPayment()
            observableEvents?.also(::observeService)
        }
        handler.onProcessExecuted(paymentConfiguration)
    }

    private fun observeService(serviceLiveData: PaymentServiceEventHandler) {
        state.observingService = true
        // Error event
        val paymentErrorLiveData: LiveData<ButtonLoadingCanceled?> =
            transform(serviceLiveData.paymentErrorLiveData) { error ->
                val shouldHandleError = error.isPaymentProcessing
                if (shouldHandleError) onPaymentProcessingError() else noRecoverableError(error)
                handler.onProcessError(error)
                ButtonLoadingCanceled
            }
        uiStateMutableLiveData.addSource(paymentErrorLiveData) { uiStateMutableLiveData.value = it }

        // Visual payment event
        val visualPaymentLiveData: LiveData<VisualProcessorResult?> =
            transform(serviceLiveData.visualPaymentLiveData) { VisualProcessorResult }
        uiStateMutableLiveData.addSource(visualPaymentLiveData) { uiStateMutableLiveData.value = it }

        // Payment finished event
        val paymentFinishedLiveData: LiveData<ButtonLoadingFinished?> =
            transform(serviceLiveData.paymentFinishedLiveData) { paymentModel ->
                state.paymentModel = paymentModel
                ButtonLoadingFinished(ExplodeDecoratorMapper(factory).map(paymentModel))
            }
        uiStateMutableLiveData.addSource(paymentFinishedLiveData) { uiStateMutableLiveData.value = it }

        // PostPayment started event
        val postPaymentStartedLiveData: LiveData<ButtonLoadingFinished?> =
            transform(serviceLiveData.postPaymentStartedLiveData) { descriptor ->
                state.iParcelablePaymentDescriptor = descriptor as? IParcelablePaymentDescriptor
                ButtonLoadingFinished()
            }
        uiStateMutableLiveData.addSource(postPaymentStartedLiveData) { uiStateMutableLiveData.value = it }

        // Invalid esc event
        val recoverRequiredLiveData: LiveData<PaymentRecovery?> =
            transform(serviceLiveData.recoverInvalidEscLiveData) {
                it.takeIf { it.shouldAskForCvv() }
            }
        this.uiStateMutableLiveData.addSource(recoverRequiredLiveData) { paymentRecovery ->
            paymentRecovery?.let { recoverPayment(it) }
            uiStateMutableLiveData.value = ButtonLoadingCanceled
        }
    }

    private fun onPaymentProcessingError() {
        val currency: Currency = paymentSettingRepository.currency
        val paymentResult: PaymentResult = PaymentResult.Builder()
            .setPaymentData(paymentDataFactory.create())
            .setPaymentStatus(Payment.StatusCodes.STATUS_IN_PROCESS)
            .setPaymentStatusDetail(Payment.StatusDetail.STATUS_DETAIL_PENDING_CONTINGENCY)
            .build()
        onPostPayment(PaymentModel(paymentResult, currency))
    }

    override fun onPostPayment(paymentModel: PaymentModel) {
        state.paymentModel = paymentModel
        uiStateMutableLiveData.value = ButtonLoadingFinished(ExplodeDecoratorMapper(factory).map(paymentModel))
    }

    override fun onPostPaymentAction(postPaymentAction: PostPaymentAction) {
        postPaymentAction.execute(object : PostPaymentAction.ActionController {
            override fun recoverPayment(postPaymentAction: PostPaymentAction) {
                uiStateMutableLiveData.value = ButtonLoadingCanceled
                recoverPayment()
            }

            override fun onChangePaymentMethod() {
                uiStateMutableLiveData.value = ButtonLoadingCanceled
            }
        })
        handler.onPostPaymentAction(postPaymentAction)
    }

    override fun skipRevealAnimation() =
        getPostPaymentConfiguration().hasPostPaymentUrl() &&
                state.iParcelablePaymentDescriptor?.paymentStatus == Payment.StatusCodes.STATUS_APPROVED

    private fun getPostPaymentConfiguration() = paymentSettingRepository
        .advancedConfiguration
        .postPaymentConfiguration

    override fun handleCongratsResult(resultCode: Int, data: Intent?) {
        handler.onPostCongrats(resultCode, data)
    }

    override fun handleSecurityCodeResult(resultCode: Int, data: Intent?) {
        handler.onPostCongrats(resultCode, data)
    }

    override fun onRecoverPaymentEscInvalid(recovery: PaymentRecovery) = recoverPayment(recovery)

    override fun recoverPayment() = recoverPayment(paymentService.createPaymentRecovery())

    private fun recoverPayment(recovery: PaymentRecovery) {
        handler.onCvvRequested(PaymentState(state.paymentConfiguration!!, paymentRecovery = recovery))
    }

    override fun manageNoConnection() {
        track(NoConnectionFrictionTracker)
        uiStateMutableLiveData.value = UIError.ConnectionError(++state.retryCounter)
    }

    private fun trackNoRecoverableFriction(error: MercadoPagoError) {
        track(
            FrictionEventTracker.with(
                OneTapViewTracker.PATH_REVIEW_ONE_TAP_VIEW,
                FrictionEventTracker.Id.GENERIC, FrictionEventTracker.Style.CUSTOM_COMPONENT, error
            )
        )
    }

    private fun noRecoverableError(error: MercadoPagoError) {
        trackNoRecoverableFriction(error)
        uiStateMutableLiveData.value = UIError.BusinessError
    }

    override fun onAnimationFinished() {
        when {
            state.paymentModel != null -> {
                handler.onProcessFinished(object : ConfirmButton.OnPaymentFinishedCallback {
                    override fun call() {
                        congratsResultLiveData.value = congratsResultFactory.create(
                            state.paymentModel!!,
                            resolvePostPaymentUrls(state.paymentModel!!)?.redirectUrl
                        )
                    }
                })
            }

            state.iParcelablePaymentDescriptor != null -> {
                if (getPostPaymentConfiguration().hasPostPaymentUrl()) {
                    val deeplink = getPostPaymentConfiguration().postPaymentDeepLinkUrl
                    postPaymentMutableLiveData.value = PostPaymentFlowStarted(
                        state.iParcelablePaymentDescriptor!!,
                        deeplink
                    )
                }
            }
        }
    }

    override fun onResultIconAnimation() {
        state.paymentModel?.paymentResult?.let { it ->
            selectPaymentSoundUseCase.execute(it,
                success = audioPlayer::play,
                failure = {
                    val frictionTrack = FrictionEventTracker.with(
                        "/audio_player",
                        FrictionEventTracker.Id.GENERIC,
                        FrictionEventTracker.Style.SCREEN,
                        it
                    )
                    tracker.track(frictionTrack)
                })
        }
    }

    override fun initState() = State()

    override fun onStateRestored() {
        if (state.observingService) {
            paymentService.observableEvents?.let {
                observeService(it)
            } ?: onPaymentProcessingError()
        }
    }

    private fun resolvePostPaymentUrls(paymentModel: PaymentModel): PostPaymentUrlsMapper.Response? {
        return paymentSettingRepository.checkoutPreference?.let { preference ->
            val congratsResponse = paymentModel.congratsResponse
            postPaymentUrlsMapper.map(
                PostPaymentUrlsMapper.Model(
                    congratsResponse.redirectUrl,
                    congratsResponse.backUrl,
                    paymentModel.payment,
                    preference,
                    paymentSettingRepository.site.id
                )
            )
        }
    }

    @Parcelize
    data class State(
        var paymentConfiguration: PaymentConfiguration? = null,
        var paymentModel: PaymentModel? = null,
        var iParcelablePaymentDescriptor: IParcelablePaymentDescriptor? = null,
        var retryCounter: Int = 0,
        var observingService: Boolean = false
    ) : BaseState
}
