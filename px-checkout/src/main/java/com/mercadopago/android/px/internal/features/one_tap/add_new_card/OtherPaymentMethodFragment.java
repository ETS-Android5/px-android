package com.mercadopago.android.px.internal.features.one_tap.add_new_card;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.presentation.extensions.ImageViewExtKt;
import com.mercadopago.android.px.internal.base.BasePagerFragment;
import com.mercadopago.android.px.internal.di.CheckoutConfigurationModule;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.features.one_tap.add_new_card.sheet_options.CardFormBottomSheetModel;
import com.mercadopago.android.px.internal.util.CardFormWrapper;
import com.mercadopago.android.px.internal.util.ListUtil;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.MPTextView;
import com.mercadopago.android.px.internal.viewmodel.drawables.OtherPaymentMethodFragmentItem;
import com.mercadopago.android.px.model.CardFormInitType;
import com.mercadopago.android.px.model.NewCardMetadata;
import com.mercadopago.android.px.model.OfflinePaymentTypesMetadata;
import com.mercadopago.android.px.model.internal.CardFormOption;
import com.mercadopago.android.px.model.internal.Text;
import java.util.List;
import kotlin.Unit;

import static com.mercadopago.android.px.internal.features.one_tap.OneTapFragment.REQ_CARD_FORM_WEB_VIEW;
import static com.mercadopago.android.px.internal.features.one_tap.OneTapFragment.REQ_CODE_CARD_FORM;
import static com.mercadopago.android.px.internal.util.AccessibilityUtilsKt.executeIfAccessibilityTalkBackEnable;

public class OtherPaymentMethodFragment
    extends BasePagerFragment<OtherPaymentMethodPresenter, OtherPaymentMethodFragmentItem>
    implements AddNewCard.View {

    private View addNewCardView;
    private View offPaymentMethodView;

    @NonNull
    public static Fragment getInstance(@NonNull final OtherPaymentMethodFragmentItem model) {
        final OtherPaymentMethodFragment instance = new OtherPaymentMethodFragment();
        instance.storeModel(model);
        return instance;
    }

    @Override
    protected OtherPaymentMethodPresenter createPresenter() {
        final CheckoutConfigurationModule configurationModule = Session.getInstance().getConfigurationModule();
        return new OtherPaymentMethodPresenter(new CardFormWrapper(
            configurationModule.getPaymentSettings(),
            configurationModule.getTrackingRepository(),
            configurationModule.getAuthorizationProvider()
        ), Session.getInstance().getTracker());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        final boolean smallMode = model.getNewCardMetadata() != null && model.getOfflineMethodsMetadata() != null;
        return smallMode ? inflater.inflate(R.layout.px_fragment_other_payment_method_small, container, false) :
            inflater.inflate(R.layout.px_fragment_other_payment_method_large, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addNewCardView = view.findViewById(R.id.px_add_new_card);
        offPaymentMethodView = view.findViewById(R.id.px_off_payment_method);
        if (model.getNewCardMetadata() != null) {
            configureAddNewCard(model.getNewCardMetadata());
        }
        if (model.getOfflineMethodsMetadata() != null) {
            configureOffMethods(model.getOfflineMethodsMetadata());
        }
    }

    private void configureAddNewCard(@NonNull final NewCardMetadata newCardMetadata) {
        addNewCardView.setVisibility(View.VISIBLE);
        final List<CardFormOption> cardFormOptions = newCardMetadata.getSheetOptions();
        final View.OnClickListener onClickListener;

        if (ListUtil.isEmpty(cardFormOptions)) {
            onClickListener = v -> presenter.onAddNewCardSelected(newCardMetadata.getCardFormInitType());
        } else {
            final Fragment parentFragment = getParentFragment();
            final CardFormBottomSheetModel model = new CardFormBottomSheetModel(
                newCardMetadata.getLabel().getMessage(),
                cardFormOptions);

            if (parentFragment instanceof OnOtherPaymentMethodClickListener) {
                final OnOtherPaymentMethodClickListener listener = ((OnOtherPaymentMethodClickListener) parentFragment);
                listener.onLoadCardFormSheetOptions(model);
                onClickListener = v -> listener.onNewCardWithSheetOptions();
            } else {
                throw new IllegalStateException(
                    "Parent fragment must implement " + OnOtherPaymentMethodClickListener.class.getSimpleName());
            }
        }

        configureViews(
            addNewCardView,
            R.drawable.px_ico_new_card,
            newCardMetadata.getLabel(),
            newCardMetadata.getDescription(),
            onClickListener);
    }

    private void configureOffMethods(@NonNull final OfflinePaymentTypesMetadata offlineMethods) {
        offPaymentMethodView.setVisibility(View.VISIBLE);
        configureViews(
            offPaymentMethodView,
            R.drawable.px_ico_off_method,
            offlineMethods.getLabel(),
            offlineMethods.getDescription(),
            v -> {
                final Fragment parentFragment = getParentFragment();
                if (parentFragment instanceof OnOtherPaymentMethodClickListener) {
                    ((OnOtherPaymentMethodClickListener) parentFragment).onOtherPaymentMethodClicked();
                } else {
                    throw new IllegalStateException(
                        "Parent fragment must implement " + OnOtherPaymentMethodClickListener.class.getSimpleName());
                }
            });
    }

    private void configureViews(@NonNull final View view, @DrawableRes final int imageResId,
        @NonNull final Text primaryMessage, @Nullable final Text secondaryMessage,
        final View.OnClickListener listener) {
        loadPrimaryMessageView(view, primaryMessage);
        loadSecondaryMessageView(view, secondaryMessage);
        loadImage(view, imageResId);
        view.setOnClickListener(listener);
        executeIfAccessibilityTalkBackEnable(view.getContext(), () -> {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            return Unit.INSTANCE;
        });
    }

    @Override
    public void setUserVisibleHint(final boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        final View view = getView();
        final ViewGroup parent = view != null ? (ViewGroup) view.getParent() : null;

        if (presenter != null && parent != null) {
            executeIfAccessibilityTalkBackEnable(parent.getContext(), () -> {
                final int modeForAccessibility =
                    isVisibleToUser ? View.IMPORTANT_FOR_ACCESSIBILITY_YES : View.IMPORTANT_FOR_ACCESSIBILITY_NO;
                parent.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null);
                offPaymentMethodView.setImportantForAccessibility(modeForAccessibility);
                addNewCardView.setImportantForAccessibility(modeForAccessibility);
                return Unit.INSTANCE;
            });
        }
    }

    protected void loadPrimaryMessageView(@NonNull final View view, @Nullable final Text primaryMessage) {
        final MPTextView primaryMessageView = view.findViewById(R.id.other_payment_method_primary_message);
        ViewUtils.loadOrHide(View.GONE, primaryMessage, primaryMessageView);
    }

    protected void loadSecondaryMessageView(@NonNull final View view, @Nullable final Text secondaryMessage) {
        final MPTextView secondaryMessageView = view.findViewById(R.id.other_payment_method_secondary_message);
        ViewUtils.loadOrHide(View.GONE, secondaryMessage, secondaryMessageView);
    }

    protected void loadImage(@NonNull final View view, @DrawableRes final int imageResId) {
        final ImageView image = view.findViewById(R.id.other_payment_method_image);
        ImageViewExtKt.loadOrGone(image, imageResId);
    }

    @Override
    public void startCardForm(@NonNull final CardFormWrapper cardFormWrapper, @NonNull final CardFormInitType initType) {
        switch (initType) {
            case STANDARD: {
                final FragmentManager manager;
                if (getParentFragment() != null && (manager = getParentFragment().getFragmentManager()) != null) {
                    cardFormWrapper.getCardFormWithFragment()
                        .start(manager, REQ_CODE_CARD_FORM, R.id.one_tap_fragment);
                }
                break;
            }
            case WEB_PAY: {
                final Fragment fragment;
                if ((fragment = getParentFragment()) != null) {
                    cardFormWrapper.getCardFormWithWebView().start(fragment, REQ_CARD_FORM_WEB_VIEW);
                }
            }
        }
    }

    public interface OnOtherPaymentMethodClickListener {
        void onLoadCardFormSheetOptions(final CardFormBottomSheetModel cardFormBottomSheetModel);

        void onNewCardWithSheetOptions();

        void onOtherPaymentMethodClicked();
    }
}