package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import com.mercadopago.android.px.model.internal.Text;
import java.io.Serializable;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public final class StatusMetadata implements Parcelable, Serializable {

    private final boolean enabled;
    @Detail private final String detail;
    private final Text mainMessage;
    private final Text secondaryMessage;
    @Nullable
    private final Text label;

    public static final Creator<StatusMetadata> CREATOR = new Creator<StatusMetadata>() {
        @Override
        public StatusMetadata createFromParcel(final Parcel in) {
            return new StatusMetadata(in);
        }

        @Override
        public StatusMetadata[] newArray(final int size) {
            return new StatusMetadata[size];
        }
    };

    protected StatusMetadata(final Parcel in) {
        enabled = in.readByte() != 0;
        detail = in.readString();
        mainMessage = in.readParcelable(Text.class.getClassLoader());
        secondaryMessage = in.readParcelable(Text.class.getClassLoader());
        label = in.readParcelable(Text.class.getClassLoader());
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeByte((byte) (enabled ? 1 : 0));
        dest.writeString(detail);
        dest.writeParcelable(mainMessage, flags);
        dest.writeParcelable(secondaryMessage, flags);
        dest.writeParcelable(label, flags);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return Detail.ACTIVE.equals(detail);
    }

    public boolean isSuspended() {
        return Detail.SUSPENDED.equals(detail);
    }

    public String getDetail() {
        return detail;
    }

    public Text getMainMessage() {
        return mainMessage;
    }

    public Text getSecondaryMessage() {
        return secondaryMessage;
    }

    @Nullable
    public Text getLabel() {
        return label;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Retention(SOURCE)
    @StringDef({ Detail.SUSPENDED, Detail.ACTIVE })
    public @interface Detail {
        String SUSPENDED = "suspended";
        String ACTIVE = "active";
    }
}