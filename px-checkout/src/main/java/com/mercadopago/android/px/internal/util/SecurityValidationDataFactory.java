package com.mercadopago.android.px.internal.util;

import androidx.annotation.NonNull;
import com.mercadopago.android.px.addons.model.EscValidationData;
import com.mercadopago.android.px.addons.model.SecurityValidationData;
import com.mercadopago.android.px.internal.core.ProductIdProvider;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import java.math.BigDecimal;

public final class SecurityValidationDataFactory {
    @NonNull final ProductIdProvider productIdProvider;
    @NonNull final BigDecimal totalAmount;
    private static final String AMOUNT_PARAM  = "amount";

    private SecurityValidationDataFactory(
        @NonNull final ProductIdProvider productIdProvider,
        @NonNull final BigDecimal totalAmount
    ) {
        this.productIdProvider = productIdProvider;
        this.totalAmount = totalAmount;
    }

    public SecurityValidationData create(@NonNull final PaymentConfiguration paymentConfiguration) {
        final boolean securityCodeRequired = paymentConfiguration.getSecurityCodeRequired();
        final EscValidationData escValidationData = new EscValidationData
            .Builder(paymentConfiguration.getCustomOptionId(), securityCodeRequired)
            .build();
        return new SecurityValidationData
            .Builder(productIdProvider.getProductId())
            .putParam(AMOUNT_PARAM, totalAmount)
            .setEscValidationData(escValidationData)
            .build();
    }

    public static SecurityValidationData create(@NonNull final ProductIdProvider productIdProvider,
        @NonNull final BigDecimal totalAmount, @NonNull final PaymentConfiguration paymentConfiguration) {
        final String productId = productIdProvider.getProductId();
        final String customOptionId = paymentConfiguration.getCustomOptionId();
        final boolean securityCodeRequired = paymentConfiguration.getSecurityCodeRequired();
        final EscValidationData escValidationData = new EscValidationData.Builder(customOptionId, securityCodeRequired)
            .build();
        return new SecurityValidationData.Builder(productId).putParam(AMOUNT_PARAM, totalAmount)
            .setEscValidationData(escValidationData).build();
    }
}