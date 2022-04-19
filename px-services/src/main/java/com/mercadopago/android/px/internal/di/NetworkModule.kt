package com.mercadopago.android.px.internal.di

import android.content.Context
import com.mercadopago.android.px.core.data.network.ConnectionHelper
import com.mercadopago.android.px.internal.adapters.NetworkApi
import com.mercadopago.android.px.internal.core.ApplicationModule
import com.mercadopago.android.px.internal.util.RetrofitUtil
import retrofit2.Retrofit

class NetworkModule(context: Context) : ApplicationModule(context) {

    val retrofitClient: Retrofit by lazy {
        RetrofitUtil.getRetrofitClient(applicationContext)
    }

    val connectionHelper by lazy {
        ConnectionHelper(applicationContext)
    }

    val networkApi by lazy {
        NetworkApi(retrofitClient, connectionHelper)
    }
}
