package com.mercadopago.android.px.model.internal

import android.os.Parcel
import com.mercadopago.android.px.internal.core.extensions.orIfNullOrEmpty
import com.mercadopago.android.px.model.ExpressMetadata

class OneTapItem(parcel: Parcel?) : ExpressMetadata(parcel) {

    private var applications: List<Application>? = null
    var offlineMethodCard: OfflineMethodCard? = null
        private set
    val id: String
        get () {
            var allPaymentMethods = ""
            getApplications().forEach {
                allPaymentMethods += it.paymentMethod.id
            }
            return allPaymentMethods + card?.id.orEmpty()
        }
    val bankTransfer: BankTransfer? = null

    init {
        throw UnsupportedOperationException("Parcelable implementation not available")
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        super.writeToParcel(dest, flags)
        throw UnsupportedOperationException("Parcelable implementation not available")
    }

    fun isOfflineMethodCard() = offlineMethodCard != null

    fun isBankTransfer() = bankTransfer != null

    fun getApplications() = applications.orIfNullOrEmpty(mutableListOf<Application>().also { applications ->
        if (isOfflineMethods) {
            offlineMethods.paymentTypes.forEach {
                it.paymentMethods.forEach { offlineMethod ->
                    applications.add(
                        Application(Application.PaymentMethod(
                            offlineMethod.id,
                            offlineMethod.instructionId),
                            listOf(),
                            offlineMethod.status)
                    )
                }
            }
        } else {
            applications.add(
                Application(Application.PaymentMethod(paymentMethodId.orEmpty(), paymentTypeId.orEmpty()), listOf(), status)
            )
        }
    })

    fun getDefaultPaymentMethodType(): String = displayInfo
        ?.cardDrawerSwitch
        ?.default
        ?: paymentTypeId ?: paymentMethodId
}
