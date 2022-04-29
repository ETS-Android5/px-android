package com.mercadopago.android.px.model.internal.remedies

import android.os.Parcelable
import com.mercadopago.android.px.model.BankTransferInfo
import kotlinx.android.parcel.Parcelize
import java.math.BigDecimal

@Parcelize
data class RemedyPaymentMethod(
    val customOptionId: String,
    val installments: Int?,
    val issuerName: String?,
    val bin: String?,
    val lastFourDigit: String?,
    val paymentMethodId: String,
    val paymentTypeId: String,
    val securityCodeLength: Int?,
    val securityCodeLocation: String?,
    val totalAmount: BigDecimal?,
    val installmentsList: List<Installment>?,
    val escStatus: String?,
    val esc: Boolean,
    val cardSize: CardSize?,
    val paymentMethodName: String?,
    val bankInfo: BankTransferInfo?
) : Parcelable