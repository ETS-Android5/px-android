package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.model.internal.CardFormOption;
import com.mercadopago.android.px.model.internal.Text;
import java.io.Serializable;
import java.util.ArrayList;

public final class NewCardMetadata implements Parcelable, Serializable {

    private final Text label;
    private final Text description;
    private final String version;
    private final CardFormInitType cardFormInitType;
    @Nullable private final String deeplink;
    @Nullable private final GenericCardDisplayInfo genericCardDisplayInfo;

    @Nullable
    private final ArrayList<CardFormOption> sheetOptions;

    public static final Creator<NewCardMetadata> CREATOR = new Creator<NewCardMetadata>() {
        @Override
        public NewCardMetadata createFromParcel(final Parcel in) {
            return new NewCardMetadata(in);
        }

        @Override
        public NewCardMetadata[] newArray(final int size) {
            return new NewCardMetadata[size];
        }
    };

    public Text getLabel() {
        return label;
    }

    public Text getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public CardFormInitType getCardFormInitType() {
        return cardFormInitType;
    }

    @Nullable
    public String getDeepLink() {
        return deeplink;
    }

    @Nullable
    public GenericCardDisplayInfo getGenericCardDisplayInfo() {
        return genericCardDisplayInfo;
    }

    @Nullable
    public ArrayList<CardFormOption> getSheetOptions() {
        return sheetOptions;
    }

    protected NewCardMetadata(final Parcel in) {
        label = in.readParcelable(Text.class.getClassLoader());
        description = in.readParcelable(Text.class.getClassLoader());
        version = in.readString();
        cardFormInitType = CardFormInitType.valueOf(in.readString());
        sheetOptions = in.readArrayList(CardFormOption.class.getClassLoader());
        deeplink = in.readString();
        genericCardDisplayInfo =  in.readParcelable(GenericCardDisplayInfo.class.getClassLoader());
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(label, flags);
        dest.writeParcelable(description, flags);
        dest.writeString(version);
        dest.writeString(cardFormInitType != null ? cardFormInitType.name() : CardFormInitType.STANDARD.name());
        dest.writeList(sheetOptions);
        dest.writeString(deeplink);
        dest.writeParcelable(genericCardDisplayInfo, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
