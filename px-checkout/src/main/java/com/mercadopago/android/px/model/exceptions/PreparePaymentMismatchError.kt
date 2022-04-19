package com.mercadopago.android.px.model.exceptions

internal class PreparePaymentMismatchError(message: String?) : MercadoPagoError(message.orEmpty(), true)
