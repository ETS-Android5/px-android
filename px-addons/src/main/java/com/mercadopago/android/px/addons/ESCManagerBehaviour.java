package com.mercadopago.android.px.addons;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.addons.model.EscDeleteReason;
import java.util.Set;
import java.util.UUID;

public interface ESCManagerBehaviour {

    void setSessionId(@NonNull final String sessionId);

    void setFlow(@NonNull final String flow);

    @Nullable
    String getESC(@Nullable final String cardId, @Nullable final String firstDigits, @Nullable final String lastDigits);

    boolean saveESCWith(@NonNull final String cardId, @NonNull final String esc);

    boolean saveESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits, @NonNull final String esc);

    /**
     * @deprecated use the one with Reason and detail parameters
     */
    @Deprecated
    void deleteESCWith(@NonNull final String cardId);

    /**
     * @deprecated there are no cases where we need to delete this ESCs
     */
    @Deprecated
    void deleteESCWith(@NonNull final String firstDigits, @NonNull final String lastDigits);

    void deleteESCWith(@NonNull final String cardId, @NonNull final EscDeleteReason reason,
        @Nullable final String detail);

    @NonNull
    Set<String> getESCCardIds();

    boolean isESCEnabled();

    @NonNull
    default String getSyncedAndroidId(@NonNull final Context context) {
        return UUID.randomUUID().toString();
    }
}
