package com.mercadopago.android.px.internal.datasource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.addons.ESCManagerBehaviour;
import com.mercadopago.android.px.internal.callbacks.MPCall;
import com.mercadopago.android.px.internal.core.AuthorizationProvider;
import com.mercadopago.android.px.internal.model.CardTokenBody;
import com.mercadopago.android.px.internal.model.RemotePaymentToken;
import com.mercadopago.android.px.internal.repository.CardTokenRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.services.GatewayService;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.Device;
import com.mercadopago.android.px.model.SavedCardToken;
import com.mercadopago.android.px.model.SavedESCCardToken;
import com.mercadopago.android.px.model.Token;
import com.mercadopago.android.px.model.exceptions.ApiException;
import com.mercadopago.android.px.model.requests.SecurityCodeIntent;
import com.mercadopago.android.px.services.Callback;

import static com.mercadopago.android.px.services.BuildConfig.API_ENVIRONMENT_NEW;

public class CardTokenService implements CardTokenRepository {

    /* default */ @NonNull final PaymentSettingRepository paymentSettingRepository;
    /* default */ @NonNull final ESCManagerBehaviour escManagerBehaviour;
    /* default */ @NonNull final AuthorizationProvider authorizationProvider;
    @NonNull private final Device device;
    @NonNull private final GatewayService gatewayService;

    public CardTokenService(@NonNull final GatewayService gatewayService,
        @NonNull final PaymentSettingRepository paymentSettingRepository,
        @NonNull final Device device,
        @NonNull final ESCManagerBehaviour escManagerBehaviour,
        @NonNull final AuthorizationProvider authorizationProvider) {
        this.gatewayService = gatewayService;
        this.paymentSettingRepository = paymentSettingRepository;
        this.device = device;
        this.escManagerBehaviour = escManagerBehaviour;
        this.authorizationProvider = authorizationProvider;
    }

    @Override
    public MPCall<Token> createToken(@NonNull final String cardId, @NonNull final String cvv,
        @Nullable final RemotePaymentToken remotePaymentToken, final boolean requireEsc) {
        final CardTokenBody body = new CardTokenBody(cardId, device, requireEsc, cvv, "", remotePaymentToken);
        return gatewayService.createToken(paymentSettingRepository.getPublicKey(), body);
    }

    @Override
    public MPCall<Token> createToken(final SavedCardToken savedCardToken) {
        savedCardToken.setDevice(device);
        return gatewayService
            .createToken(paymentSettingRepository.getPublicKey(),
                savedCardToken);
    }

    @Override
    public MPCall<Token> createToken(final SavedESCCardToken savedESCCardToken) {
        savedESCCardToken.setDevice(device);
        return gatewayService
            .createToken(paymentSettingRepository.getPublicKey(),
                savedESCCardToken);
    }

    @Override
    public MPCall<Token> cloneToken(final String tokenId) {
        return gatewayService
            .cloneToken(tokenId, paymentSettingRepository.getPublicKey());
    }

    @Override
    public MPCall<Token> putSecurityCode(final String securityCode, final String tokenId) {
        final SecurityCodeIntent securityCodeIntent = new SecurityCodeIntent();
        securityCodeIntent.setSecurityCode(securityCode);
        return gatewayService
            .updateToken(tokenId, paymentSettingRepository.getPublicKey(),
                securityCodeIntent);
    }

    @Override
    public void clearCap(@NonNull final String cardId, @NonNull final ClearCapCallback callback) {
        if (TextUtil.isEmpty(authorizationProvider.getPrivateKey())) {
            callback.execute();
            return;
        }
        gatewayService.clearCap(cardId)
            .enqueue(new Callback<String>() {
                @Override
                public void success(final String s) {
                    callback.execute();
                }

                @Override
                public void failure(final ApiException apiException) {
                    callback.execute();
                }
            });
    }

    @Override
    public MPCall<Token> createToken(@NonNull final Card card, @Nullable final RemotePaymentToken remotePaymentToken) {
        return null;
    }

    @Override
    public MPCall<Token> createTokenWithoutCvv(@NonNull final Card card,
        @Nullable final RemotePaymentToken remotePaymentToken) {
        return null;
    }
}