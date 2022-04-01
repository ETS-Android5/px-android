package com.mercadopago.android.px.internal.features.one_tap.slider;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import com.meli.android.carddrawer.model.CardDrawerView;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.presentation.extensions.ViewExtKt;
import com.mercadopago.android.px.internal.di.MapperProvider;
import com.mercadopago.android.px.internal.features.payment_result.remedies.RemediesLinkableMapper;
import com.mercadopago.android.px.internal.util.ViewUtils;
import com.mercadopago.android.px.internal.view.LinkableTextView;
import com.mercadopago.android.px.internal.viewmodel.DisableConfiguration;
import com.mercadopago.android.px.internal.viewmodel.drawables.ConsumerCreditsDrawableFragmentItem;
import com.mercadopago.android.px.model.ConsumerCreditsDisplayInfo;

public class ConsumerCreditsFragment extends PaymentMethodFragment<ConsumerCreditsDrawableFragmentItem> {

    private ConstraintLayout creditsLagout;
    private ImageView background;
    private ImageView logo;
    private LinkableTextView topText;
    private LinkableTextView bottomText;
    private final RemediesLinkableMapper remediesLinkableMapper = MapperProvider.INSTANCE.getRemediesLinkableMapper();

    @NonNull
    public static Fragment getInstance(final ConsumerCreditsDrawableFragmentItem model) {
        final ConsumerCreditsFragment instance = new ConsumerCreditsFragment();
        instance.storeModel(model);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_consumer_credits, container, false);
    }

    @Override
    public void initializeViews(@NonNull final View view) {
        super.initializeViews(view);
        creditsLagout = view.findViewById(R.id.credits_layout);
        background = view.findViewById(R.id.background);
        logo = view.findViewById(R.id.logo);
        topText = view.findViewById(R.id.top_text);
        bottomText = view.findViewById(R.id.bottom_text);
        final ConsumerCreditsDisplayInfo displayInfo = model.metadata.displayInfo;
        tintBackground(background, displayInfo.color);
        showDisplayInfo(displayInfo);
        configureListener();
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
    }

    @Override
    protected void updateCardDrawerView(@NonNull final CardDrawerView cardDrawerView) { }

    public void showDisplayInfo(@NonNull final ConsumerCreditsDisplayInfo displayInfo) {
        if (topText != null) {
            topText.updateModel(remediesLinkableMapper.map(displayInfo.topText));
        }

        if (bottomText != null) {
            bottomText.updateModel(remediesLinkableMapper.map(displayInfo.bottomText));
        }
    }

    public void configureListener() {
        final Fragment parent = getParentFragment();
        LinkableTextView.LinkableTextListener listener = null;
        if (parent instanceof LinkableTextView.LinkableTextListener) {
            listener = (LinkableTextView.LinkableTextListener) parent;
        }

        if (listener != null && topText != null) {
            topText.setLinkableTextListener(listener);
        }

        if (listener != null && bottomText != null) {
            bottomText.setLinkableTextListener(listener);
        }
    }

    @Override
    public void disable() {
        super.disable();
        final DisableConfiguration disableConfiguration = new DisableConfiguration(getContext());
        ViewExtKt.grayScaleViewGroup(creditsLagout);
        background.clearColorFilter();
        background.setImageResource(0);
        background.setBackgroundColor(disableConfiguration.getBackgroundColor());
        if (topText != null) {
            topText.setVisibility(View.GONE);
        }
        bottomText.setVisibility(View.GONE);
        centerLogo();
    }

    private void centerLogo() {
        final ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(creditsLagout);
        constraintSet.connect(logo.getId(), ConstraintSet.LEFT, creditsLagout.getId(), ConstraintSet.LEFT, 0);
        constraintSet.connect(logo.getId(), ConstraintSet.RIGHT, creditsLagout.getId(), ConstraintSet.RIGHT, 0);
        constraintSet.connect(logo.getId(), ConstraintSet.TOP, creditsLagout.getId(), ConstraintSet.TOP, 0);
        constraintSet.connect(logo.getId(), ConstraintSet.BOTTOM, creditsLagout.getId(), ConstraintSet.BOTTOM, 0);
        constraintSet.applyTo(creditsLagout);
    }

    @Override
    protected String getAccessibilityContentDescription() {
        return model.getCommonsByApplication().getCurrent().getDescription();
    }
}