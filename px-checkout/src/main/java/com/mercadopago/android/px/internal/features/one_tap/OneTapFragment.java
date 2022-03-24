package com.mercadopago.android.px.internal.features.one_tap;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import com.mercadolibre.android.andesui.bottomsheet.AndesBottomSheet;
import com.mercadolibre.android.cardform.CardForm;
import com.mercadolibre.android.cardform.internal.CardFormWithFragment;
import com.mercadolibre.android.cardform.internal.LifecycleListener;
import com.mercadopago.android.px.R;
import com.mercadopago.android.px.core.BackHandler;
import com.mercadopago.android.px.core.DynamicDialogCreator;
import com.mercadopago.android.px.internal.base.BaseFragment;
import com.mercadopago.android.px.internal.di.CheckoutConfigurationModule;
import com.mercadopago.android.px.internal.di.FactoryProvider;
import com.mercadopago.android.px.internal.di.MapperProvider;
import com.mercadopago.android.px.internal.di.Session;
import com.mercadopago.android.px.internal.experiments.ScrolledVariant;
import com.mercadopago.android.px.internal.experiments.Variant;
import com.mercadopago.android.px.internal.experiments.VariantHandler;
import com.mercadopago.android.px.internal.features.TermsAndConditionsActivity;
import com.mercadopago.android.px.internal.features.disable_payment_method.DisabledPaymentMethodDetailDialog;
import com.mercadopago.android.px.internal.features.one_tap.add_new_card.OtherPaymentMethodFragment;
import com.mercadopago.android.px.internal.features.one_tap.add_new_card.sheet_options.CardFormBottomSheetFragment;
import com.mercadopago.android.px.internal.features.one_tap.add_new_card.sheet_options.CardFormBottomSheetModel;
import com.mercadopago.android.px.internal.features.one_tap.animations.ExpandAndCollapseAnimation;
import com.mercadopago.android.px.internal.features.one_tap.animations.FadeAnimationListener;
import com.mercadopago.android.px.internal.features.one_tap.animations.FadeAnimator;
import com.mercadopago.android.px.internal.features.one_tap.installments.InstallmentRowHolder;
import com.mercadopago.android.px.internal.features.one_tap.installments.InstallmentsAdapter;
import com.mercadopago.android.px.internal.features.one_tap.installments.InstallmentsAdapterV2;
import com.mercadopago.android.px.internal.features.one_tap.offline_methods.OfflineMethodsFragment;
import com.mercadopago.android.px.internal.features.one_tap.slider.ConfirmButtonAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.HubAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.PaymentMethodFragment;
import com.mercadopago.android.px.internal.features.one_tap.slider.PaymentMethodFragmentAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.PaymentMethodHeaderAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.SplitPaymentHeaderAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.SummaryViewAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.TitlePagerAdapter;
import com.mercadopago.android.px.internal.features.one_tap.slider.TitlePagerAdapterV2;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialog;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialogAction;
import com.mercadopago.android.px.internal.features.generic_modal.GenericDialogItem;
import com.mercadopago.android.px.internal.features.pay_button.PayButton;
import com.mercadopago.android.px.internal.features.pay_button.PayButtonFragment;
import com.mercadopago.android.px.internal.features.security_code.SecurityCodeFragment;
import com.mercadopago.android.px.internal.util.CardFormWrapper;
import com.mercadopago.android.px.internal.util.ErrorUtil;
import com.mercadopago.android.px.internal.util.Logger;
import com.mercadopago.android.px.internal.util.VibrationUtils;
import com.mercadopago.android.px.internal.view.DiscountDetailDialog;
import com.mercadopago.android.px.internal.view.ElementDescriptorView;
import com.mercadopago.android.px.internal.view.LabeledSwitch;
import com.mercadopago.android.px.internal.view.LinkableTextView;
import com.mercadopago.android.px.internal.view.PaymentMethodHeaderView;
import com.mercadopago.android.px.internal.view.ScrollingPagerIndicator;
import com.mercadopago.android.px.internal.view.SummaryView;
import com.mercadopago.android.px.internal.view.TitlePager;
import com.mercadopago.android.px.internal.view.animator.OneTapTransition;
import com.mercadopago.android.px.internal.view.experiments.ExperimentHelper;
import com.mercadopago.android.px.internal.viewmodel.GenericColor;
import com.mercadopago.android.px.internal.viewmodel.PostPaymentAction;
import com.mercadopago.android.px.internal.viewmodel.SplitSelectionState;
import com.mercadopago.android.px.internal.viewmodel.drawables.DrawableFragmentItem;
import com.mercadopago.android.px.model.Currency;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.TermsAndConditionsLinks;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.Site;
import com.mercadopago.android.px.model.StatusMetadata;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.Application;
import com.mercadopago.android.px.model.internal.DisabledPaymentMethod;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class OneTapFragment extends BaseFragment implements OneTap.View, ViewPager.OnPageChangeListener,
    SplitPaymentHeaderAdapter.SplitListener, PaymentMethodFragment.DisabledDetailDialogLauncher,
    OtherPaymentMethodFragment.OnOtherPaymentMethodClickListener, PayButton.Handler, GenericDialog.Listener,
    BackHandler, PaymentMethodFragment.PaymentMethodPagerListener, LinkableTextView.LinkableTextListener {

    private static final String TAG = OneTapFragment.class.getSimpleName();
    private static final String TAG_HEADER_DYNAMIC_DIALOG = "TAG_HEADER_DYNAMIC_DIALOG";
    private static final String EXTRA_VARIANT = "EXTRA_VARIANT";
    private static final String EXTRA_FROM_DEEPLINK = "FROM_DEEPLINK";
    private static final String EXTRA_RENDER_MODE = "render_mode";
    private static final String EXTRA_NAVIGATION_STATE = "navigation_state";

    private static final int REQ_CODE_DISABLE_DIALOG = 105;
    private static final float PAGER_NEGATIVE_MARGIN_MULTIPLIER = -1.5f;
    public static final int REQ_CARD_FORM_WEB_VIEW = 953;
    public static final int REQ_CODE_CARD_FORM = 106;

    @Nullable private CallBack callback;
    @NonNull /* default */ OneTap.NavigationState navigationState = OneTap.NavigationState.NONE;
    @Nullable private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks;

    /* default */ OneTapPresenter presenter;

    private SummaryView summaryView;
    private View payButtonContainer;
    private View oneTapContainer;
    private RecyclerView installmentsRecyclerView;
    /* default */ ViewPager paymentMethodPager;
    /* default */ View pagerAndConfirmButtonContainer;
    /* default */ RenderMode renderMode;
    private ScrollingPagerIndicator indicator;
    @Nullable private ExpandAndCollapseAnimation expandAndCollapseAnimation;
    @Nullable private FadeAnimator fadeAnimation;
    @Nullable private Animation slideUpAndFadeAnimation;
    @Nullable private Animation slideDownAndFadeAnimation;
    private InstallmentsAdapter installmentsAdapter;
    private PaymentMethodHeaderView paymentMethodHeaderView;
    private TitlePagerAdapter titlePagerAdapter;
    private LabeledSwitch splitPaymentView;
    private PaymentMethodFragmentAdapter paymentMethodFragmentAdapter;
    private View loading;
    private AndesBottomSheet cardFormBottomSheet;
    private OneTapTransition transition;

    private HubAdapter hubAdapter;

    private PayButtonFragment payButtonFragment;
    private OfflineMethodsFragment offlineMethodsFragment;

    public static Fragment getInstance(@NonNull final Variant variant, final boolean fromDeeplink) {
        final OneTapFragment oneTapFragment = new OneTapFragment();
        final Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_VARIANT, variant);
        bundle.putBoolean(EXTRA_FROM_DEEPLINK, fromDeeplink);
        oneTapFragment.setArguments(bundle);
        return oneTapFragment;
    }

    @Override
    public void onSplitChanged(final boolean isChecked) {
        presenter.onSplitChanged(isChecked);
    }

    @Override
    public void getViewTrackPath(@NonNull final PayButton.ViewTrackPathCallback callback) {
        presenter.onGetViewTrackPath(callback);
    }

    @Override
    public void prePayment(@NonNull final PayButton.OnReadyForPaymentCallback callback) {
        presenter.handlePrePaymentAction(callback);
    }

    @Override
    public void onPaymentExecuted(@NonNull final PaymentConfiguration configuration) {
        presenter.onPaymentExecuted(configuration);
    }

    @Override
    public void onPostPaymentAction(@NonNull final PostPaymentAction postPaymentAction) {
        navigationState = OneTap.NavigationState.NONE;
        presenter.onPostPaymentAction(postPaymentAction);
    }

    @NonNull
    @Override
    public PayButton.CvvRequestedModel onCvvRequested() {
        return new PayButton.CvvRequestedModel(R.id.one_tap_fragment, renderMode);
    }

    @Override
    public boolean handleBack() {
        final boolean isExploding = payButtonFragment.isExploding();
        if (!isExploding) {
            presenter.onBack();
        }
        return isExploding || offlineMethodsFragment.handleBack();
    }

    @Override
    public void onApplicationChanged(@NonNull final String paymentTypeId) {
        presenter.onApplicationChanged(paymentTypeId);
    }

    @Override
    public void onLinkClicked(@NonNull final TermsAndConditionsLinks installmentLink) {
        final int installmentKey = titlePagerAdapter.getCurrentInstallment();
        final String data = installmentLink.getLinkByInstallment(installmentKey);
        final Context context = getActivity();

        if (context != null) {
            TermsAndConditionsActivity.start(context, data);
        }
    }

    public interface CallBack {
        void onOneTapCanceled();
    }

    @Nullable
    @Override
    public Animation onCreateAnimation(final int transit, final boolean enter, final int nextAnim) {
        if (navigationState == OneTap.NavigationState.CARD_FORM) {
            if (enter) {
                navigationState = OneTap.NavigationState.NONE;
                transition.playEnterFromCardForm();
            } else {
                transition.playExitToCardForm();
            }
        } else if (navigationState == OneTap.NavigationState.SECURITY_CODE) {
            if (enter) {
                navigationState = OneTap.NavigationState.NONE;
                transition.playEnterFromSecurityCode();
            } else {
                transition.playExitToSecurityCode();
            }
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
        @Nullable final ViewGroup container,
        @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.px_fragment_one_tap_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        configureViews(view);
        transition = new OneTapTransition(paymentMethodPager, summaryView, payButtonContainer,
            paymentMethodHeaderView, indicator, splitPaymentView, oneTapContainer);

        presenter = createPresenter();
        if (savedInstanceState != null) {
            renderMode = (RenderMode) savedInstanceState.getSerializable(EXTRA_RENDER_MODE);
            navigationState =
                (OneTap.NavigationState) savedInstanceState.getSerializable(EXTRA_NAVIGATION_STATE);
            if (savedInstanceState.getParcelable(BUNDLE_STATE) != null) {
                presenter.restoreState(savedInstanceState.getParcelable(BUNDLE_STATE));
            }
        } else {
            presenter.onFreshStart();
            if (getArguments() != null && getArguments().getBoolean(EXTRA_FROM_DEEPLINK)) {
                onDeepLinkReceived();
            }
        }

        presenter.attachView(this);

        summaryView.setOnLogoClickListener(v -> presenter.onHeaderClicked());

        paymentMethodPager.addOnPageChangeListener(this);

        fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentAttached(@NonNull final FragmentManager fm, @NonNull final Fragment fragment,
                @NonNull final Context context) {
                super.onFragmentAttached(fm, fragment, context);
                if (fragment.getTag().equals(CardFormWithFragment.TAG)) {
                    navigationState = OneTap.NavigationState.CARD_FORM;
                } else if (fragment instanceof SecurityCodeFragment) {
                    navigationState = OneTap.NavigationState.SECURITY_CODE;
                }
            }
        };
        getActivity().getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (fragmentLifecycleCallbacks != null) {
            getActivity().getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
        }
    }

    private void configureViews(@NonNull final View view) {
        configurePaymentMethodHeader(view);
        oneTapContainer = view.findViewById(R.id.one_tap_container);
        payButtonFragment = (PayButtonFragment) getChildFragmentManager().findFragmentById(R.id.pay_button);
        payButtonContainer = view.findViewById(R.id.pay_button);
        offlineMethodsFragment =
            (OfflineMethodsFragment) getChildFragmentManager().findFragmentById(R.id.offline_methods);
        splitPaymentView = view.findViewById(R.id.labeledSwitch);
        summaryView = view.findViewById(R.id.summary_view);
        loading = view.findViewById(R.id.loading);
        cardFormBottomSheet = view.findViewById(R.id.new_card_sheet_options);
        pagerAndConfirmButtonContainer = view.findViewById(R.id.container);
        paymentMethodPager = view.findViewById(R.id.payment_method_pager);
        indicator = view.findViewById(R.id.indicator);

        paymentMethodPager.setPageMargin(
            ((int) (getResources().getDimensionPixelSize(R.dimen.px_m_margin) * PAGER_NEGATIVE_MARGIN_MULTIPLIER)));
        paymentMethodPager.setOffscreenPageLimit(2);
        slideDownAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, INVISIBLE));
        slideUpAndFadeAnimation.setAnimationListener(new FadeAnimationListener(paymentMethodPager, VISIBLE));

        if (getActivity() instanceof AppCompatActivity) {
            summaryView.configureToolbar((AppCompatActivity) getActivity(), v -> presenter.cancel());
        }

        hubAdapter = new HubAdapter(Arrays.asList(titlePagerAdapter,
            new SummaryViewAdapter(summaryView),
            new SplitPaymentHeaderAdapter(splitPaymentView, this),
            new PaymentMethodHeaderAdapter(paymentMethodHeaderView),
            new ConfirmButtonAdapter(payButtonFragment)
        ));
    }

    @Override
    public void configurePayButton(@NonNull final PayButton.StateChange listener) {
        payButtonFragment.addOnStateChange(listener);
    }

    private void configurePaymentMethodHeader(@NonNull final View view) {
        final Bundle arguments = getArguments();
        if (arguments == null || !arguments.containsKey(EXTRA_VARIANT)) {
            throw new IllegalStateException("One tap should have a variant to display");
        }
        final Variant variant = Objects.requireNonNull(arguments.getParcelable(EXTRA_VARIANT));
        ExperimentHelper.INSTANCE.applyExperimentViewBy(
            view.findViewById(R.id.installments_header_experiment_container), variant, getLayoutInflater());

        paymentMethodHeaderView = view.findViewById(R.id.installments_header);
        paymentMethodHeaderView.setListener(new PaymentMethodHeaderView.Listener() {
            @Override
            public void onDescriptorViewClicked() {
                presenter.onInstallmentsRowPressed();
            }

            @Override
            public void onInstallmentsSelectorCancelClicked() {
                presenter.onInstallmentSelectionCanceled();
            }

            @Override
            public void onDisabledDescriptorViewClick() {
                presenter.onDisabledDescriptorViewClick();
            }

            @Override
            public void onInstallmentViewUpdated() {
                presenter.updateInstallments();
            }
        });

        installmentsRecyclerView = view.findViewById(R.id.installments_recycler_view);
        final TitlePager titlePager = view.findViewById(R.id.title_pager);
        variant.process(new VariantHandler() {
            @Override
            public void visit(@NonNull final ScrolledVariant variant) {
                if (variant.isDefault()) {
                    titlePagerAdapter = new TitlePagerAdapter(titlePager);
                    installmentsAdapter = new InstallmentsAdapter(OneTapFragment.this::onInstallmentSelected);
                    expandAndCollapseAnimation = new ExpandAndCollapseAnimation(installmentsRecyclerView);
                    installmentsRecyclerView.setVisibility(GONE);
                } else {
                    titlePagerAdapter = new TitlePagerAdapterV2(titlePager);
                    installmentsAdapter = new InstallmentsAdapterV2(OneTapFragment.this::onInstallmentSelected);
                }
            }
        });
        titlePager.setAdapter(titlePagerAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        installmentsRecyclerView.setLayoutManager(linearLayoutManager);
        installmentsRecyclerView
            .addItemDecoration(new DividerItemDecoration(view.getContext(), linearLayoutManager.getOrientation()));

        installmentsRecyclerView.setAdapter(installmentsAdapter);

        paymentMethodHeaderView.setBackgroundColor(
            new GenericColor(R.color.px_checkout_payment_method_header_background).getColor(requireContext()));
        installmentsRecyclerView.setBackgroundColor(
            new GenericColor(R.color.px_checkout_payment_method_header_background).getColor(requireContext()));
    }

    private OneTapPresenter createPresenter() {
        final Session session = Session.getInstance();
        final CheckoutConfigurationModule configurationModule = session.getConfigurationModule();
        return new OneTapPresenter(configurationModule.getPaymentSettings(),
            configurationModule.getDisabledPaymentMethodRepository(),
            configurationModule.getPayerCostSelectionRepository(),
            configurationModule.getApplicationSelectionRepository(),
            session.getDiscountRepository(),
            session.getAmountRepository(),
            session.getUseCaseModule().getCheckoutUseCase(),
            session.getUseCaseModule().getCheckoutWithNewCardUseCase(),
            session.getAmountConfigurationRepository(),
            session.getConfigurationModule().getChargeRepository(),
            session.getMercadoPagoESC(),
            session.getExperimentsRepository(),
            configurationModule.getTrackingRepository(), configurationModule.getCustomTextsRepository(),
            session.getOneTapItemRepository(), session.getPayerPaymentMethodRepository(), session.getModalRepository(),
            session.getCustomOptionIdSolver(),
            MapperProvider.INSTANCE.getPaymentMethodDrawableItemMapper(),
            MapperProvider.INSTANCE.getPaymentMethodDescriptorMapper(),
            MapperProvider.INSTANCE.getSummaryDetailDescriptorMapper(),
            MapperProvider.INSTANCE.getSummaryInfoMapper(),
            MapperProvider.INSTANCE.getElementDescriptorMapper(),
            MapperProvider.INSTANCE.getFromApplicationToApplicationInfo(),
            configurationModule.getAuthorizationProvider(),
            FactoryProvider.INSTANCE.getAmountDescriptorViewModelFactory(),
            session.getTracker()
        );
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putSerializable(EXTRA_RENDER_MODE, renderMode);
        outState.putSerializable(EXTRA_NAVIGATION_STATE, navigationState);
        if (presenter != null) {
            outState.putParcelable(BUNDLE_STATE, presenter.getState());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(@NonNull final Context context) {
        super.onAttach(context);
        fadeAnimation = new FadeAnimator(context);
        slideDownAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_down_and_fade);
        slideUpAndFadeAnimation = AnimationUtils.loadAnimation(context, R.anim.px_slide_up_and_fade);
        if (context instanceof CallBack) {
            callback = (CallBack) context;
        }
    }

    @Override
    public void onDetach() {
        callback = null;
        fadeAnimation = null;
        expandAndCollapseAnimation = null;
        slideDownAndFadeAnimation = null;
        slideUpAndFadeAnimation = null;
        if (presenter != null) {
            presenter.detachView();
        }
        super.onDetach();
    }

    @Override
    public void clearAdapters() {
        paymentMethodPager.setAdapter(null);
    }

    @Override
    public void configureRenderMode(@NonNull final List<Variant> variants) {
        for (final Variant variant : variants) {
            variant.process(new VariantHandler() {
                @Override
                public void visit(@NonNull final ScrolledVariant variant) {
                    if (!variant.isDefault()) {
                        renderMode = RenderMode.DYNAMIC;
                    }
                }
            });
        }
    }

    @Override
    public void configureAdapters(@NonNull final Site site, @NonNull final Currency currency) {

        // Order is important, should update all others adapters before update paymentMethodAdapter

        if (paymentMethodPager.getAdapter() == null) {
            paymentMethodFragmentAdapter = new PaymentMethodFragmentAdapter(getChildFragmentManager());
            if (renderMode == null) {
                summaryView.setMeasureListener((itemsClipped) -> {
                    renderMode = itemsClipped ? RenderMode.LOW_RES : RenderMode.HIGH_RES;
                    onRenderModeDecided();
                });
            } else {
                onRenderModeDecided();
            }
            paymentMethodPager.setAdapter(paymentMethodFragmentAdapter);
            indicator.attachToPager(paymentMethodPager);
        }
    }

    @Override
    public void updateAdapters(@NonNull final HubAdapter.Model model) {
        hubAdapter.update(model);
    }

    @Override
    public void updatePaymentMethods(@NonNull final List<DrawableFragmentItem> items) {
        paymentMethodFragmentAdapter.setItems(items);
    }

    @Override
    public void cancel() {
        if (callback != null) {
            callback.onOneTapCanceled();
        }
    }

    @Override
    public void updateInstallmentsList(final int index, @NonNull final List<InstallmentRowHolder.Model> models) {
        installmentsRecyclerView.scrollToPosition(index);
        installmentsAdapter.setModels(models);
        installmentsAdapter.setPayerCostSelected(index);
        installmentsAdapter.notifyDataSetChanged();
    }

    @Override
    public void animateInstallmentsList() {
        animateViewPagerDown();
        hubAdapter.showInstallmentsList();
        if (expandAndCollapseAnimation != null) {
            expandAndCollapseAnimation.expand();
        }
    }

    private void animateViewPagerDown() {
        paymentMethodPager.startAnimation(slideDownAndFadeAnimation);
        fadeAnimation.fadeOutFast(payButtonContainer);
        fadeAnimation.fadeOutFast(indicator);
    }

    @Override
    public void showToolbarElementDescriptor(@NonNull final ElementDescriptorView.Model elementDescriptorModel) {
        summaryView.showToolbarElementDescriptor(elementDescriptorModel);
    }

    @Override
    public void showDisabledPaymentMethodDetailDialog(@NonNull final DisabledPaymentMethod disabledPaymentMethod,
        @NonNull final StatusMetadata currentStatus) {
        DisabledPaymentMethodDetailDialog
            .showDialog(this, REQ_CODE_DISABLE_DIALOG, disabledPaymentMethod.getPaymentStatusDetail(), currentStatus);
    }

    @Override
    public void collapseInstallmentsSelection() {
        if (expandAndCollapseAnimation != null && expandAndCollapseAnimation.shouldCollapse()) {
            paymentMethodPager.startAnimation(slideUpAndFadeAnimation);
            fadeAnimation.fadeIn(payButtonContainer);
            fadeAnimation.fadeIn(indicator);
            expandAndCollapseAnimation.collapse();
        }
        paymentMethodFragmentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (requestCode == ErrorUtil.ERROR_REQUEST_CODE) {
            cancel();
        } else if (requestCode == REQ_CODE_DISABLE_DIALOG) {
            setPagerIndex(0);
        } else if (requestCode == REQ_CODE_CARD_FORM) {
            handleCardFormResult(resultCode);
        } else if (requestCode == REQ_CARD_FORM_WEB_VIEW) {
            handlerCardFormWebViewResult(resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void handleCardFormResult(final int resultCode) {
        if (resultCode == RESULT_OK) {
            presenter.onCardFormResult();
        }
    }

    private void handlerCardFormWebViewResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            final String cardId = data.getStringExtra(CardForm.RESULT_CARD_ID_KEY);
            showLoading();
            presenter.onCardAdded(cardId, new LifecycleListener.Callback() {
                @Override
                public void onSuccess() {
                    presenter.onCardFormResult();
                    hideLoading();
                }

                @Override
                public void onError() {
                    hideLoading();
                }
            });
        }
    }

    @Override
    public void updateViewForPosition(final int paymentMethodIndex,
        final int payerCostSelected, @NonNull final SplitSelectionState splitSelectionState,
        @NonNull final Application application) {
        hubAdapter.updateData(paymentMethodIndex, payerCostSelected, splitSelectionState, application);
    }

    /* default */ void onInstallmentSelected(final PayerCost payerCostSelected) {
        presenter.onPayerCostSelected(payerCostSelected);
    }

    @Override
    public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
        hubAdapter.updatePosition(positionOffset, position);
    }

    @Override
    public void onPageSelected(final int position) {
        presenter.onSliderOptionSelected(position);
        VibrationUtils.smallVibration(getContext());
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
        // do nothing.
    }

    @Override
    public void showDiscountDetailDialog(@NonNull final Currency currency,
        @NonNull final DiscountConfigurationModel discountModel) {
        DiscountDetailDialog.showDialog(getChildFragmentManager(), discountModel);
    }

    @Override
    public void setPagerIndex(final int index) {
        paymentMethodPager.setCurrentItem(index);
    }

    @Override
    public void showDynamicDialog(@NonNull final DynamicDialogCreator creator,
        @NonNull final DynamicDialogCreator.CheckoutData checkoutData) {
        final Context context;
        if ((context = getContext()) != null && creator.shouldShowDialog(context, checkoutData)) {
            creator.create(getContext(), checkoutData).show(getChildFragmentManager(),
                TAG_HEADER_DYNAMIC_DIALOG);
        }
    }

    private void onRenderModeDecided() {
        //Workaround to pager not updating the fragments
        paymentMethodPager.post(() -> paymentMethodFragmentAdapter.setRenderMode(renderMode));
    }

    @Override
    public int getRequestCode() {
        return REQ_CODE_DISABLE_DIALOG;
    }

    @Override
    public void onOtherPaymentMethodClicked() {
        presenter.onOtherPaymentMethodClicked();
    }

    @Override
    public void onLoadCardFormSheetOptions(final CardFormBottomSheetModel cardFormBottomSheetModel) {
        final CardFormBottomSheetFragment cardFormSheetContainer =
            CardFormBottomSheetFragment.newInstance(cardFormBottomSheetModel);
        cardFormSheetContainer.setCardFormOptionClick(() -> cardFormBottomSheet.collapse());
        cardFormBottomSheet.setContent(
            getChildFragmentManager(),
            cardFormSheetContainer,
            null);
    }

    @Override
    public void onNewCardWithSheetOptions() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> cardFormBottomSheet.expand(), 200);
    }

    @Override
    public void showOfflineMethodsExpanded() {
        offlineMethodsFragment.showExpanded();
    }

    @Override
    public void showOfflineMethodsCollapsed() {
        offlineMethodsFragment.showCollapsed();
    }

    @Override
    public void onAction(@NonNull final GenericDialogAction action) {
        if (action instanceof GenericDialogAction.DeepLinkAction) {
            startDeepLink(((GenericDialogAction.DeepLinkAction) action).getDeepLink());
        } else if (action instanceof GenericDialogAction.CustomAction) {
            presenter.handleGenericDialogAction(((GenericDialogAction.CustomAction) action).getType());
        }
    }

    @Override
    public void showGenericDialog(@NonNull final GenericDialogItem item) {
        GenericDialog.showDialog(getChildFragmentManager(), item);
    }

    @Override
    public void startAddNewCardFlow(final CardFormWrapper cardFormWrapper) {
        final FragmentManager manager = getFragmentManager();
        if (manager != null) {
            cardFormWrapper.getCardFormWithFragment()
                .start(manager, REQ_CODE_CARD_FORM, R.id.one_tap_fragment);
        }
    }

    @Override
    public void startDeepLink(@NonNull final String deepLink) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)));
        } catch (final ActivityNotFoundException e) {
            Logger.debug(TAG, e);
        }
    }

    @Override
    public void onDeepLinkReceived() {
        presenter.handleDeepLink();
    }

    @Override
    public void showLoading() {
        loading.setVisibility(VISIBLE);
    }

    @Override
    public void hideLoading() {
        loading.setVisibility(GONE);
    }

    @Override
    public void configurePaymentMethodHeader(@NonNull final List<Variant> variants) {
        paymentMethodHeaderView.configureExperiment(variants);
    }

    @Override
    public void showError(@NonNull final MercadoPagoError mercadoPagoError) {
        ErrorUtil.startErrorActivity(this, mercadoPagoError);
    }
}
