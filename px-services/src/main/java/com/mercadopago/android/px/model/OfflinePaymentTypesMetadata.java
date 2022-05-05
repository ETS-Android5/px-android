package com.mercadopago.android.px.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.mercadopago.android.px.model.internal.Text;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public final class OfflinePaymentTypesMetadata implements Parcelable, Serializable {

    private final Text label;
    private final Text description;
    private final List<OfflinePaymentType> paymentTypes;
    @Nullable private final OfflinePaymentTypeDisplayInfo displayInfo;
    @Nullable private final GenericCardDisplayInfo genericCardDisplayInfo;

    public static final Creator<OfflinePaymentTypesMetadata> CREATOR = new Creator<OfflinePaymentTypesMetadata>() {
        @Override
        public OfflinePaymentTypesMetadata createFromParcel(final Parcel in) {
            return new OfflinePaymentTypesMetadata(in);
        }

        @Override
        public OfflinePaymentTypesMetadata[] newArray(final int size) {
            return new OfflinePaymentTypesMetadata[size];
        }
    };

    @NonNull
    public Text getLabel() {
        return label;
    }

    @Nullable
    public Text getDescription() {
        return description;
    }

    @NonNull
    public List<OfflinePaymentType> getPaymentTypes() {
        return paymentTypes != null ? paymentTypes : Collections.emptyList();
    }

    @Nullable
    public OfflinePaymentTypeDisplayInfo getDisplayInfo() {
        return displayInfo;
    }

    @Nullable
    public GenericCardDisplayInfo getGenericCardDisplayInfo() {
        return genericCardDisplayInfo;
    }

    protected OfflinePaymentTypesMetadata(final Parcel in) {
        label = in.readParcelable(Text.class.getClassLoader());
        description = in.readParcelable(Text.class.getClassLoader());
        paymentTypes = in.createTypedArrayList(OfflinePaymentType.CREATOR);
        displayInfo = in.readParcelable(OfflinePaymentTypeDisplayInfo.class.getClassLoader());
        genericCardDisplayInfo = in.readParcelable(GenericCardDisplayInfo.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeParcelable(label, flags);
        dest.writeParcelable(description, flags);
        dest.writeTypedList(paymentTypes);
        dest.writeParcelable(displayInfo, flags);
        dest.writeParcelable(genericCardDisplayInfo, flags);
    }
}
