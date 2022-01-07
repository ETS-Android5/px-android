package com.mercadopago.android.px.internal.features.payment_congrats

import com.mercadopago.android.px.internal.base.BaseState
import com.mercadopago.android.px.internal.base.BaseViewModelWithState
import com.mercadopago.android.px.internal.core.ConnectionHelper
import com.mercadopago.android.px.internal.livedata.MediatorSingleLiveData
import com.mercadopago.android.px.internal.repository.CongratsRepository
import com.mercadopago.android.px.internal.repository.PaymentRepository
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository
import com.mercadopago.android.px.internal.viewmodel.PaymentModel
import com.mercadopago.android.px.model.IPaymentDescriptor
import com.mercadopago.android.px.tracking.internal.MPTracker
import com.mercadopago.android.px.tracking.internal.events.NoConnectionFrictionTracker
import kotlinx.android.parcel.Parcelize

internal class CongratsViewModel(
    private val congratsRepository: CongratsRepository,
    private val paymentRepository: PaymentRepository,
    private val congratsResultFactory: CongratsResultFactory,
    private val connectionHelper: ConnectionHelper,
    paymentSettingRepository: PaymentSettingRepository,
    tracker: MPTracker
) : BaseViewModelWithState<CongratsViewModel.State>(tracker), CongratsRepository.PostPaymentCallback {

    val congratsResultLiveData = MediatorSingleLiveData<CongratsResult>()

    override fun initState() = State()

    init {
        paymentSettingRepository.advancedConfiguration.postPaymentConfiguration.cleanPostPaymentDeepLinkUrl()
    }

    fun createCongratsResult(iPaymentDescriptor: IPaymentDescriptor?) {
        congratsResultLiveData.value = CongratsPostPaymentResult.Loading(true)

        if (connectionHelper.hasConnection()) {
            val descriptor = iPaymentDescriptor ?: paymentRepository.payment
            if (descriptor != null) {
                val paymentResult = paymentRepository.createPaymentResult(descriptor)
                congratsRepository.getPostPaymentData(descriptor, paymentResult, this@CongratsViewModel)
            } else {
                congratsResultLiveData.value = CongratsPostPaymentResult.Loading(false)
                congratsResultLiveData.value = CongratsPostPaymentResult.BusinessError
            }
        } else {
            congratsResultLiveData.value = CongratsPostPaymentResult.Loading(false)
            manageNoConnection()
        }
    }

    override fun handleResult(paymentModel: PaymentModel) {
        congratsResultLiveData.value = CongratsPostPaymentResult.Loading(false)
        congratsResultLiveData.value = congratsResultFactory.create(paymentModel)
    }

    private fun manageNoConnection() {
        track(NoConnectionFrictionTracker)
        congratsResultLiveData.value = CongratsPostPaymentResult.ConnectionError
    }

    @Parcelize
    data class State(
        var iPaymentDescriptor: IPaymentDescriptor? = null,
        var retryCounter: Int = 0
    ) : BaseState
}
