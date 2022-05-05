package com.mercadopago.android.px.tracking.internal.views

import com.mercadopago.android.px.model.OfflinePaymentType
import com.mercadopago.android.px.tracking.internal.model.AvailableOfflineMethod
import com.mercadopago.android.px.tracking.internal.model.TrackingMapModel

internal class OfflineMethodsData : TrackingMapModel() {

    val availableMethods: MutableMap<String, Any?> = mutableMapOf()

    companion object {
        fun createFrom(offlinePaymentTypes: List<OfflinePaymentType>): MutableMap<String, Any?> {
            val offlineMethodsData = OfflineMethodsData()
            offlineMethodsData.availableMethods.also { instance ->
                instance[AVAILABLE_METHODS] = mutableListOf<AvailableOfflineMethod>().also { list ->
                    for (offlinePaymentType in offlinePaymentTypes) {
                        for (offlinePaymentMethod in offlinePaymentType.paymentMethods) {
                            list.add(AvailableOfflineMethod(offlinePaymentType.id, offlinePaymentMethod.id))
                        }
                    }
                }
            }

            return offlineMethodsData.availableMethods
        }
        private const val AVAILABLE_METHODS = "available_methods"
    }

}
