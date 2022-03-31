package com.mercadopago.android.px.internal.util;

import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.exceptions.NoConnectivityException;
import java.io.IOException;
import retrofit2.Response;

public final class ApiUtil {

    private ApiUtil() {
    }

    public static <T> ApiException getApiException(final Response<T> response) {
        ApiException apiException = null;
        try {
            final String errorString = response.errorBody().string();
            apiException = JsonUtil.fromJson(errorString, ApiException.class);
        } catch (final Exception ex) {
            //Do nothing
        } finally {
            if (apiException == null) {
                apiException = new ApiException();
                apiException.setStatus(response.code());
            }
        }

        return apiException;
    }

    public static ApiException getApiException(final Throwable throwable) {
        final ApiException apiException = new ApiException();
        if (throwable instanceof NoConnectivityException) {
            apiException.setStatus(StatusCodes.NO_CONNECTIVITY_ERROR);
        } else if (throwable instanceof IOException) {
            apiException.setStatus(StatusCodes.GENERIC_TIME_OUT);
        }

        try {
            apiException.setMessage(throwable.getMessage());
        } catch (final Exception ex) {
            // do nothing
        }

        return apiException;
    }

    public static final class StatusCodes {

        private StatusCodes() {
        }

        public static final int CACHE_FAIL = -3;
        private static final int GENERIC_TIME_OUT = -2;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int PROCESSING = 499;
        public static final int BAD_REQUEST = 400;
        public static final int NOT_FOUND = 404;
        public static final int NO_CONNECTIVITY_ERROR = -1;
        public static final String INTERNAL_SERVER_ERROR_FIRST_DIGIT = "5";
    }

    public static final class RequestOrigin {

        private RequestOrigin() {
        }

        public static final String POST_INIT = "POST_INIT";
        public static final String CREATE_PAYMENT = "CREATE_PAYMENT";
        public static final String CREATE_TOKEN = "CREATE_TOKEN";
        public static final String PREPARE_PAYMENT = "PREPARE_PAYMENT";
        public static final String GET_INSTRUCTIONS = "GET_INSTRUCTIONS";
    }
}
