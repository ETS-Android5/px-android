package com.mercadopago.android.px.model

import java.io.Serializable

data class TransactionInfo(val bankInfo: BankInfo?, val financialInstitutionId: String?) : Serializable