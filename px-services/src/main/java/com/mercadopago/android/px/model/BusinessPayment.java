package com.mercadopago.android.px.model;

import android.os.Bundle;
import android.os.Parcel;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.mercadopago.android.px.internal.util.ListUtil;
import com.mercadopago.android.px.internal.util.TextUtil;
import com.mercadopago.android.px.model.internal.PrimaryExitAction;
import com.mercadopago.android.px.model.internal.SecondaryExitAction;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class BusinessPayment implements IParcelablePaymentDescriptor {

    @NonNull private final String title;
    @NonNull private final Decorator decorator;
    @NonNull private final String paymentStatus;
    @NonNull private final String paymentStatusDetail;
    private final boolean shouldShowPaymentMethod;
    private final int iconId;

    @Nullable private final String help;
    @Nullable private final ExitAction exitActionPrimary;
    @Nullable private final ExitAction exitActionSecondary;
    @Nullable private final String statementDescription;
    @Nullable private final String receiptId;

    @Nullable private final String imageUrl;
    @Nullable private final ExternalFragment topFragment;
    @Nullable private final ExternalFragment bottomFragment;
    @Nullable private final ExternalFragment importantFragment;

    @Nullable private final String subtitle;
    @Nullable private final String paymentTypeId;
    @Nullable private final String paymentMethodId;

    @Nullable private final List<String> receiptIdList;
    private final boolean shouldShowReceipt;
    private final boolean forceShowReceipt;

    /* default */ BusinessPayment(final Builder builder) {
        help = builder.help;
        title = builder.title;
        decorator = builder.decorator;
        iconId = builder.iconId;
        shouldShowPaymentMethod = builder.shouldShowPaymentMethod;
        exitActionPrimary = builder.buttonPrimary;
        exitActionSecondary = builder.buttonSecondary;
        statementDescription = builder.statementDescription;
        receiptId = builder.receiptId;
        imageUrl = builder.imageUrl;
        topFragment = builder.topFragment;
        bottomFragment = builder.bottomFragment;
        importantFragment = builder.importantFragment;
        paymentStatus = builder.paymentStatus;
        paymentStatusDetail = builder.paymentStatusDetail;
        subtitle = builder.subtitle;
        paymentMethodId = builder.paymentMethodId;
        paymentTypeId = builder.paymentTypeId;
        receiptIdList = builder.receiptIdList;
        shouldShowReceipt = builder.shouldShowReceipt;
        forceShowReceipt = builder.forceShowReceipt;
    }

    protected BusinessPayment(final Parcel in) {
        iconId = in.readInt();
        title = in.readString();
        shouldShowPaymentMethod = in.readByte() != 0;
        exitActionPrimary = in.readParcelable(ExitAction.class.getClassLoader());
        exitActionSecondary = in.readParcelable(ExitAction.class.getClassLoader());
        decorator = Decorator.fromName(in.readString());
        help = in.readString();
        statementDescription = in.readString();
        receiptId = in.readString();
        imageUrl = in.readString();
        topFragment = in.readParcelable(ExternalFragment.class.getClassLoader());
        bottomFragment = in.readParcelable(ExternalFragment.class.getClassLoader());
        importantFragment = in.readParcelable(ExternalFragment.class.getClassLoader());
        paymentStatus = in.readString();
        paymentStatusDetail = in.readString();
        subtitle = in.readString();
        paymentMethodId = in.readString();
        paymentTypeId = in.readString();
        receiptIdList = in.createStringArrayList();
        shouldShowReceipt = in.readByte() != 0;
        forceShowReceipt = in.readByte() != 0;
    }

    public static final Creator<BusinessPayment> CREATOR = new Creator<BusinessPayment>() {
        @Override
        public BusinessPayment createFromParcel(final Parcel in) {
            return new BusinessPayment(in);
        }

        @Override
        public BusinessPayment[] newArray(final int size) {
            return new BusinessPayment[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeInt(iconId);
        dest.writeString(title);
        dest.writeByte((byte) (shouldShowPaymentMethod ? 1 : 0));
        dest.writeParcelable(exitActionPrimary, flags);
        dest.writeParcelable(exitActionSecondary, flags);
        dest.writeString(decorator.name);
        dest.writeString(help);
        dest.writeString(statementDescription);
        dest.writeString(receiptId);
        dest.writeString(imageUrl);
        dest.writeParcelable(topFragment, 0);
        dest.writeParcelable(bottomFragment, 0);
        dest.writeParcelable(importantFragment, 0);
        dest.writeString(paymentStatus);
        dest.writeString(paymentStatusDetail);
        dest.writeString(subtitle);
        dest.writeString(paymentMethodId);
        dest.writeString(paymentTypeId);
        dest.writeStringList(receiptIdList);
        dest.writeByte((byte) (shouldShowReceipt ? 1 : 0));
        dest.writeByte((byte) (forceShowReceipt ? 1 : 0));
    }

    public boolean hasReceipt() {
        return getReceipt() != null;
    }

    public boolean hasTopFragment() {
        return getTopFragment() != null;
    }

    public boolean hasBottomFragment() {
        return getBottomFragment() != null;
    }

    public boolean hasImportantFragment() {
        return getImportantFragment() != null;
    }

    @NonNull
    public Decorator getDecorator() {
        return decorator;
    }

    public int getIcon() {
        return iconId;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public boolean hasHelp() {
        return TextUtil.isNotEmpty(help);
    }

    @Nullable
    public ExitAction getSecondaryAction() {
        return exitActionSecondary != null ? new SecondaryExitAction(exitActionSecondary) : null;
    }

    @Nullable
    public ExitAction getPrimaryAction() {
        return exitActionPrimary != null ? new PrimaryExitAction(exitActionPrimary) : null;
    }

    @Nullable
    public String getHelp() {
        return help;
    }

    public boolean shouldShowPaymentMethod() {
        return shouldShowPaymentMethod;
    }

    @Nullable
    @Override
    public Long getId() {
        final String receiptId = getReceipt();
        return receiptId != null ? Long.parseLong(receiptId) : null;
    }

    @Override
    @Nullable
    public String getStatementDescription() {
        return statementDescription;
    }

    @Nullable
    public String getReceipt() {
        return ListUtil.isNotEmpty(receiptIdList) ? receiptIdList.get(0) : receiptId;
    }

    @Nullable
    public String getImageUrl() {
        return imageUrl;
    }

    @Nullable
    public ExternalFragment getTopFragment() {
        return topFragment;
    }

    @Nullable
    public ExternalFragment getBottomFragment() {
        return bottomFragment;
    }

    @Nullable
    public ExternalFragment getImportantFragment() {
        return importantFragment;
    }

    @Override
    @NonNull
    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    @NonNull
    public String getPaymentStatusDetail() {
        return paymentStatusDetail;
    }

    @Nullable
    public String getSubtitle() {
        return subtitle;
    }

    @Nullable
    @Override
    public String getPaymentTypeId() {
        return paymentTypeId;
    }

    @Nullable
    @Override
    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    @Nullable
    @Override
    public List<String> getPaymentIds() {
        return ListUtil.isNotEmpty(receiptIdList) ? receiptIdList :
            (TextUtil.isNotEmpty(receiptId) ? Collections.singletonList(receiptId) : null);
    }

    public boolean shouldShowReceipt() {
        return shouldShowReceipt;
    }

    public boolean forceShowReceipt() {
        return forceShowReceipt;
    }

    @Override
    public void process(@NonNull final IPaymentDescriptorHandler handler) {
        handler.visit(this);
    }

    public enum Decorator {
        APPROVED("APPROVED"),
        REJECTED("REJECTED"),
        PENDING("PENDING");

        public final String name;

        Decorator(final String name) {
            this.name = name;
        }

        public static Decorator fromName(final String text) {
            for (final Decorator s : Decorator.values()) {
                if (s.name.equalsIgnoreCase(text)) {
                    return s;
                }
            }
            throw new IllegalStateException("Invalid decorator");
        }
    }

    public static final class Builder {

        // Mandatory values
        /* default */ @NonNull final Decorator decorator;
        /* default */ @DrawableRes final int iconId;
        /* default */ @NonNull final String title;
        /* default */ @NonNull final String paymentStatus;
        /* default */ @NonNull final String paymentStatusDetail;
        /* default */ @Nullable final String paymentMethodId;
        /* default */ @Nullable final String paymentTypeId;

        // Optional values
        /* default */ @Nullable String imageUrl;
        /* default */ boolean shouldShowPaymentMethod;
        /* default */ @Nullable String statementDescription;
        /* default */ @Nullable ExitAction buttonPrimary;
        /* default */ @Nullable ExitAction buttonSecondary;
        /* default */ @Nullable String help;
        /* default */ @Nullable String receiptId;
        /* default */ @Nullable String subtitle;
        /* default */ @Nullable List<String> receiptIdList;
        /* default */ boolean shouldShowReceipt = true;
        /* default */ boolean forceShowReceipt = false;

        /* default */ ExternalFragment topFragment;
        /* default */ ExternalFragment bottomFragment;
        /* default */ ExternalFragment importantFragment;

        @Deprecated
        public Builder(@NonNull final Decorator decorator,
            @NonNull final String paymentStatus,
            @NonNull final String paymentStatusDetail,
            @DrawableRes final int iconId,
            @NonNull final String title) {
            this(decorator, paymentStatus, paymentStatusDetail, iconId, title, null, null);
        }

        @Deprecated
        public Builder(@NonNull final Decorator decorator,
            @NonNull final String paymentStatus,
            @NonNull final String paymentStatusDetail,
            @NonNull final String imageUrl,
            @NonNull final String title) {
            this(decorator, paymentStatus, paymentStatusDetail, imageUrl, title, null, null);
        }

        public Builder(@NonNull final Decorator decorator,
            @NonNull final String paymentStatus,
            @NonNull final String paymentStatusDetail,
            @DrawableRes final int iconId,
            @NonNull final String title,
            @NonNull final String paymentMethodId,
            @NonNull final String paymentTypeId) {
            this.decorator = decorator;
            this.paymentStatus = paymentStatus;
            this.paymentStatusDetail = paymentStatusDetail;
            this.iconId = iconId;
            this.title = title;
            this.paymentMethodId = paymentMethodId;
            this.paymentTypeId = paymentTypeId;
        }

        public Builder(@NonNull final Decorator decorator,
            @NonNull final String paymentStatus,
            @NonNull final String paymentStatusDetail,
            @NonNull final String imageUrl,
            @NonNull final String title,
            @NonNull final String paymentMethodId,
            @NonNull final String paymentTypeId) {
            this(decorator, paymentStatus, paymentStatusDetail, 0, title, paymentMethodId, paymentTypeId);
            this.imageUrl = imageUrl;
        }

        public BusinessPayment build() {
            if (buttonPrimary == null && buttonSecondary == null) {
                throw new IllegalStateException("At least one button should be provided for BusinessPayment");
            }
            return new BusinessPayment(this);
        }

        /**
         * if Exit action is set, then a big primary button will appear and the click action will trigger a resCode that
         * will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setPrimaryButton(@Nullable final ExitAction exitAction) {
            buttonPrimary = exitAction;
            return this;
        }

        /**
         * if Exit action is set, then a small secondary button will appear and the click action will trigger a resCode
         * that will be the same of the Exit action added.
         *
         * @param exitAction a {@link ExitAction }
         * @return builder
         */
        public Builder setSecondaryButton(@Nullable final ExitAction exitAction) {
            buttonSecondary = exitAction;
            return this;
        }

        /**
         * if help is set, then a small box with help instructions will appear
         *
         * @param help a help message
         * @return builder
         */
        public Builder setHelp(@Nullable final String help) {
            this.help = help;
            return this;
        }

        /**
         * If value true is set, then payment method box will appear with the amount value and payment method options
         * that were selected by the user.
         *
         * @param visible visibility mode
         * @return builder
         */
        public Builder setPaymentMethodVisibility(final boolean visible) {
            shouldShowPaymentMethod = visible;
            return this;
        }

        /**
         * If value true is set on {@link #setPaymentMethodVisibility } and the payment method is credit card then the
         * statementDescription will be shown on payment method view.
         *
         * @param statementDescription disclaimer text
         * @return builder
         */
        public Builder setStatementDescription(final String statementDescription) {
            this.statementDescription = statementDescription;
            return this;
        }

        /**
         * If value is set, then receipt view will appear.
         *
         * @param receiptId the receipt id to be shown.
         * @return builder
         */
        public Builder setReceiptId(final String receiptId) {
            this.receiptId = receiptId;
            return this;
        }

        /**
         * Custom fragment that will appear before payment method description inside Business result screen.
         *
         * @param zClass fragment class
         * @return builder
         */
        public Builder setTopFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            topFragment = new ExternalFragment(zClass, args);
            return this;
        }

        /**
         * Custom fragment that will appear after payment method description inside Business result screen.
         *
         * @param zClass fragment class
         * @return builder
         */
        public Builder setBottomFragment(@NonNull final Class<? extends Fragment> zClass, @Nullable final Bundle args) {
            bottomFragment = new ExternalFragment(zClass, args);
            return this;
        }

        /**
         * Custom fragment that will appear at the top of all information inside Business result screen.
         *
         * @param zClass fragment class
         * @return builder
         */
        public Builder setImportantFragment(@NonNull final Class<? extends Fragment> zClass,
            @Nullable final Bundle args) {
            importantFragment = new ExternalFragment(zClass, args);
            return this;
        }

        /**
         * When subtitle is set, then default {@link Decorator} subtitle will be replaced on the screen with it.
         *
         * @param subtitle subtitle text
         * @return builder
         */
        public Builder setSubtitle(@Nullable final String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        /**
         * @param receiptIdList The list of receipt ids
         * @return builder
         */
        public Builder setReceiptIdList(@NonNull final List<String> receiptIdList) {
            this.receiptIdList = receiptIdList;
            return this;
        }

        /**
         * Override the receipt drawing, without depending on the receipt id
         *
         * @param shouldShowReceipt if the receipt should be drawn
         * @return builder
         */
        public Builder setShouldShowReceipt(final boolean shouldShowReceipt) {
            this.shouldShowReceipt = shouldShowReceipt;
            return this;
        }

        /**
         * Override the receipt drawing, without depending on the receipt status
         *
         * @param forceShowReceipt if the receipt should be drawn independent of its status
         * @return builder
         */
        public Builder setForceShowReceipt(final boolean forceShowReceipt) {
            this.forceShowReceipt = forceShowReceipt;
            return this;
        }
    }
}