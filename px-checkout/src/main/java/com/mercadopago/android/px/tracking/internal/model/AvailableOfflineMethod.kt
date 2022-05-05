package com.mercadopago.android.px.tracking.internal.model

import androidx.annotation.Keep

@Keep
class AvailableOfflineMethod(
    val paymentMethodType: String,
    val paymentMethodId: String
) : TrackingMapModel()
