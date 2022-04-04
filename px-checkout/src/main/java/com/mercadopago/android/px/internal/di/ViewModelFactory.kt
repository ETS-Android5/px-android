package com.mercadopago.android.px.internal.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mercadopago.android.px.internal.base.FragmentCommunicationViewModel
import com.mercadopago.android.px.internal.features.one_tap.offline_methods.OfflineMethodsViewModel
import com.mercadopago.android.px.internal.features.pay_button.PayButtonViewModel
import com.mercadopago.android.px.internal.features.payment_congrats.CongratsViewModel
import com.mercadopago.android.px.internal.features.security_code.SecurityCodeViewModel
import com.mercadopago.android.px.internal.features.security_code.mapper.TrackingParamModelMapper
import com.mercadopago.android.px.internal.mappers.CardUiMapper
import com.mercadopago.android.px.internal.mappers.PayButtonViewModelMapper

internal class ViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val session = Session.getInstance()
        val configurationModule = session.configurationModule
        val paymentSetting = configurationModule.paymentSettings
        val useCaseModule = session.useCaseModule

        return when {
            modelClass.isAssignableFrom(PayButtonViewModel::class.java) -> {
                PayButtonViewModel(
                    session.congratsResultFactory,
                    session.paymentRepository,
                    configurationModule.productIdProvider,
                    session.networkModule.connectionHelper,
                    paymentSetting,
                    configurationModule.customTextsRepository,
                    PayButtonViewModelMapper(),
                    MapperProvider.getPostPaymentUrlsMapper(),
                    MapperProvider.getRenderModeMapper(session.applicationContext),
                    useCaseModule.playSoundUseCase,
                    session.paymentResultViewModelFactory,
                    session.tracker
                )
            }
            modelClass.isAssignableFrom(OfflineMethodsViewModel::class.java) -> {
                OfflineMethodsViewModel(
                    paymentSetting,
                    session.amountRepository,
                    session.discountRepository,
                    session.oneTapItemRepository,
                    session.configurationModule.payerComplianceRepository,
                    session.tracker
                )
            }
            modelClass.isAssignableFrom(SecurityCodeViewModel::class.java) -> {
                SecurityCodeViewModel(
                    useCaseModule.tokenizeUseCase,
                    useCaseModule.displayDataUseCase,
                    useCaseModule.securityTrackModelUseCase,
                    TrackingParamModelMapper(),
                    CardUiMapper,
                    session.tracker
                )
            }
            modelClass.isAssignableFrom(FragmentCommunicationViewModel::class.java) -> {
                FragmentCommunicationViewModel(session.tracker)
            }
            modelClass.isAssignableFrom(CongratsViewModel::class.java) -> {
                CongratsViewModel(
                    session.congratsRepository,
                    session.paymentRepository,
                    session.congratsResultFactory,
                    session.networkModule.connectionHelper,
                    paymentSetting,
                    MapperProvider.getPostPaymentUrlsMapper(),
                    session.tracker
                )
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        } as T
    }
}
