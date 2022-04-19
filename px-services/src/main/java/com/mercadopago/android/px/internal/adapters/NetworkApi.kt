package com.mercadopago.android.px.internal.adapters

import com.mercadopago.android.px.core.data.network.ConnectionHelper
import com.mercadopago.android.px.internal.base.CoroutineContextProvider
import com.mercadopago.android.px.internal.base.exception.ExceptionFactory
import com.mercadopago.android.px.internal.base.exception.ExceptionParser
import com.mercadopago.android.px.internal.callbacks.ApiResponse
import com.mercadopago.android.px.internal.callbacks.ApiResponse.Failure
import com.mercadopago.android.px.internal.callbacks.ApiResponse.Success
import com.mercadopago.android.px.model.exceptions.ApiException
import com.mercadopago.android.px.model.exceptions.SocketTimeoutApiException
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit

internal typealias ApiResponseCallback<R> = ApiResponse<R, ApiException>

class NetworkApi(
    private val retrofitClient: Retrofit,
    private val connectionHelper: ConnectionHelper,
    private val contextProvider: CoroutineContextProvider = CoroutineContextProvider()
) {
    suspend fun <D : Any, T> apiCallForResponse(
        apiServiceClass: Class<T>,
        apiCall: suspend (api: T) -> Response<D>
    ): ApiResponseCallback<D> {
        return withContext(contextProvider.IO) {
            apiCallWithRetries(apiServiceClass, apiCall)
        }
    }

    private fun <D : Any> resolveCallBackResponse(response: Response<D>): ApiResponseCallback<D> {
        return runCatching {
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Success(body)
            } else {
                var failure = Failure(ExceptionFactory.genericError())
                response.errorBody()?.also {
                    failure = Failure(ExceptionParser.parse(response))
                }

                failure
            }
        }.getOrElse { Failure(ExceptionParser.parse(it)) }
    }

    private suspend fun <D : Any, T> apiCallWithRetries(
        apiServiceClass: Class<T>,
        apiCall: suspend (api: T) -> Response<D>
    ): ApiResponseCallback<D> {
        var currAttempt = 1
        var apiResponse = getApiResponse(apiCall, apiServiceClass)
        while (needRetry(apiResponse, currAttempt)) {
            if (!connectionHelper.hasConnection()) {
                return Failure(ExceptionFactory.connectionError())
            }
            apiResponse = getApiResponse(apiCall, apiServiceClass)
            currAttempt++
        }
        return apiResponse
    }

    private fun <D : Any> needRetry(apiResponse: ApiResponseCallback<D>, currAttempt: Int) =
        apiResponse is Failure && currAttempt <= MAX_RETRIES && apiResponse.exception !is SocketTimeoutApiException

    private suspend fun <D : Any, T> getApiResponse(
        apiCall: suspend (api: T) -> Response<D>,
        apiServiceClass: Class<T>
    ): ApiResponseCallback<D> {
        return runCatching {
            apiCall(retrofitClient.create(apiServiceClass))
        }.map(::resolveCallBackResponse).getOrElse {
            Failure(ExceptionParser.parse(it))
        }
    }

    companion object {
        private const val MAX_RETRIES = 3
    }
}
