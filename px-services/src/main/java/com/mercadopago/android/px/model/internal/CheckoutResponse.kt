package com.mercadopago.android.px.model.internal

import com.google.gson.annotations.SerializedName
import com.mercadopago.android.px.addons.model.internal.Experiment
import com.mercadopago.android.px.model.*
import com.mercadopago.android.px.preferences.CheckoutPreference

data class CheckoutResponse(
    val site: Site,
    val currency: Currency,
    @SerializedName("one_tap")
    val oneTapItems: List<OneTapItem>,
    @SerializedName("configurations")
    val configuration: Configuration,
    val modals: Map<String, Modal>,
    val availablePaymentMethods: List<PaymentMethod>,
    val payerPaymentMethods: List<CustomSearchItem>,
    @SerializedName("general_coupon")
    val defaultAmountConfiguration: String,
    @SerializedName("coupons")
    val discountsConfigurations: Map<String, DiscountConfigurationModel>,
    // TODO: Make non-nullable when backend has IDC ready
    val customCharges: Map<String, CustomChargeDM>? = null, // Maps payment type id to charges
    val preference: CheckoutPreference? = null,
    val experiments: List<Experiment>? = null,
    val payerCompliance: PayerCompliance? = null,
    val retry: Retry? = null
)
