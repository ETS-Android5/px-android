package com.mercadopago.android.px.addons.tokenization

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

const val TOKENIZATION_RESPONSE = "tokenization_response"

open class Tokenize {

    protected var sessionId: String? = null
    protected var showLoading: Boolean? = null

    fun sessionId(sessionId: String) = apply {
        this.sessionId = sessionId
    }

    fun showLoading(showLoading: Boolean) = apply {
        this.showLoading = showLoading
    }

    open fun start(fragment: Fragment, requestCode: Int) {
        DummyTokenizationActivity.start(fragment, requestCode)
    }

    open fun start(activity: AppCompatActivity, requestCode: Int) {
        DummyTokenizationActivity.start(activity, requestCode)
    }
}
