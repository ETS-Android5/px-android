package com.mercadopago.android.px.model.internal

import com.google.gson.annotations.SerializedName

data class CheckoutFeaturesDM(
    @SerializedName("one_tap")
    val express: Boolean,
    val split: Boolean,
    @SerializedName("odr")
    val odrFlag: Boolean,
    val comboCard: Boolean,
    val hybridCard: Boolean,
    val pix: Boolean,
    val customTaxesCharges: Boolean,
    val cardsCustomTaxesCharges: Boolean,
    val taxableCharges: Boolean,
    val styleVersion: String,
    val threedsSdkVersion: String,
    @SerializedName("validations_programs")
    val validationPrograms: List<String>,
    val debin: String
)
