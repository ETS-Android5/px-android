package com.mercadopago.android.px.model.internal

import com.google.gson.annotations.SerializedName
import com.mercadopago.android.px.preferences.CheckoutPreference

data class InitRequestBody(
    val publicKey: String,
    val cardsWithEsc: Collection<String>,
    val charges: Collection<PaymentTypeChargeRuleDM>,
    @SerializedName("discount_configuration")
    val discountParamsConfiguration: DiscountParamsConfigurationDM,
    val features: CheckoutFeaturesDM,
    val paymentMethodBehaviours: List<PaymentMethodBehaviourDM>,
    val checkoutType: CheckoutType,
    val preferenceId: String?,
    val preference: CheckoutPreference?,
    val flow: String?,
    val newCardId: String?,
    val newBankAccountId: String?,
    @SerializedName("payment_methods_rulesets")
    val paymentMethodRuleSet: List<String>
)
