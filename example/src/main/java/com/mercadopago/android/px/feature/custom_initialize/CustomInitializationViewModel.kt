package com.mercadopago.android.px.feature.custom_initialize

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mercadopago.SampleApplication
import com.mercadopago.SamplePaymentProcessor
import com.mercadopago.android.px.configuration.AdvancedConfiguration
import com.mercadopago.android.px.configuration.PaymentConfiguration
import com.mercadopago.android.px.core.MercadoPagoCheckout
import com.mercadopago.android.px.di.preference.InitializationDataPreferences
import com.mercadopago.android.px.internal.datasource.MercadoPagoPaymentConfiguration
import com.mercadopago.android.px.internal.util.TextUtil
import com.mercadopago.android.px.utils.PaymentUtils.getGenericPaymentApproved

internal class CustomInitializationViewModel(private val preferences: InitializationDataPreferences) : ViewModel() {

    private val initializationData = preferences.getInitializationData()

    val uiStateLiveData = MutableLiveData<CustomInitializeState>()

    fun recoverFromBundle(bundle: Bundle) {
        initializationData.updateModel(
            bundle.getString(EXTRA_LOCALE)!!,
            bundle.getString(EXTRA_PUBLIC_KEY)!!,
            bundle.getString(EXTRA_PREFERENCE_ID)!!,
            bundle.getString(EXTRA_ACCESS_TOKEN)!!,
            bundle.getBoolean(EXTRA_ONE_TAP),
            bundle.getString(EXTRA_PROCESSOR_TYPE)!!
        )
    }

    fun storeInBundle(bundle: Bundle) {
        bundle.putString(EXTRA_LOCALE, initializationData.locale.value)
        bundle.putString(EXTRA_PUBLIC_KEY, initializationData.publicKey.value)
        bundle.putString(EXTRA_PREFERENCE_ID, initializationData.preferenceId.value)
        bundle.putString(EXTRA_ACCESS_TOKEN, initializationData.accessToken.value)
        bundle.putBoolean(EXTRA_ONE_TAP, initializationData.oneTap.value)
        bundle.putString(EXTRA_PROCESSOR_TYPE, initializationData.processor.value.name)
    }

    fun initialize() {
        updateView()
    }

    fun onConfigurationChanged(data: ConfigurationDataType) {
        initializationData.updateModel(data)
    }

    private fun updateView() {
        uiStateLiveData.value = CustomInitializeState.LoadData(initializationData)
    }

    fun onClear() {
        initializationData.updateModel(TextUtil.EMPTY, TextUtil.EMPTY, TextUtil.EMPTY, TextUtil.EMPTY, true,
            ProcessorType.DEFAULT.name)
        updateView()
    }

    fun onStartButtonClicked() {
        preferences.saveInitializationData(initializationData)
        initializationData.locale.value.takeIf { it.isNotEmpty() }?.let { SampleApplication.localeTag = it }

        val advancedConfiguration = AdvancedConfiguration.Builder()
            .setExpressPaymentEnable(initializationData.oneTap.value)
            .build()

        val paymentConfiguration: PaymentConfiguration = when (initializationData.processor.value) {
            ProcessorType.DEFAULT -> MercadoPagoPaymentConfiguration.create()
            ProcessorType.VISUAL ->
                PaymentConfiguration.Builder(SamplePaymentProcessor(true, getGenericPaymentApproved())).build()
            ProcessorType.NO_VISUAL ->
                PaymentConfiguration.Builder(SamplePaymentProcessor(false, getGenericPaymentApproved())).build()
        }

        val builder = MercadoPagoCheckout.Builder(
            initializationData.publicKey.value.replace(TAGGED_INPUT_REGEX, TextUtil.EMPTY),
            initializationData.preferenceId.value.replace(TAGGED_INPUT_REGEX, TextUtil.EMPTY),
            paymentConfiguration)
            .setPrivateKey(initializationData.accessToken.value.replace(TAGGED_INPUT_REGEX, TextUtil.EMPTY))
            .setAdvancedConfiguration(advancedConfiguration)
        uiStateLiveData.value = CustomInitializeState.InitCheckout(builder)
    }

    companion object {
        val TAGGED_INPUT_REGEX = Regex("\\[(.*?)\\] *")
        const val EXTRA_LOCALE = "locale"
        const val EXTRA_PUBLIC_KEY = "public_key"
        const val EXTRA_PREFERENCE_ID = "preference_id"
        const val EXTRA_ACCESS_TOKEN = "access_token"
        const val EXTRA_ONE_TAP = "one_tap"
        const val EXTRA_PROCESSOR_TYPE = "processor_type"
    }
}
