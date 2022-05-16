package com.mercadopago.android.px.internal.datasource;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import com.mercadopago.android.px.configuration.AdvancedConfiguration;
import com.mercadopago.android.px.configuration.PostPaymentConfiguration;
import com.mercadopago.android.px.core.internal.CheckoutData;
import com.mercadopago.android.px.core.internal.PaymentWrapper;
import com.mercadopago.android.px.core.v2.PaymentProcessor;
import com.mercadopago.android.px.internal.callbacks.PaymentServiceHandler;
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithEscUseCase;
import com.mercadopago.android.px.internal.base.use_case.TokenizeWithoutCvvUseCase;
import com.mercadopago.android.px.internal.core.FileManager;
import com.mercadopago.android.px.internal.features.validation_program.ValidationProgramUseCase;
import com.mercadopago.android.px.internal.model.SecurityType;
import com.mercadopago.android.px.internal.repository.AmountConfigurationRepository;
import com.mercadopago.android.px.internal.repository.ApplicationSelectionRepository;
import com.mercadopago.android.px.internal.repository.CongratsRepository;
import com.mercadopago.android.px.internal.repository.DisabledPaymentMethodRepository;
import com.mercadopago.android.px.internal.repository.EscPaymentManager;
import com.mercadopago.android.px.internal.repository.PayerCostSelectionRepository;
import com.mercadopago.android.px.internal.repository.PaymentSettingRepository;
import com.mercadopago.android.px.internal.repository.UserSelectionRepository;
import com.mercadopago.android.px.internal.util.PaymentConfigurationUtil;
import com.mercadopago.android.px.mocks.CheckoutResponseStub;
import com.mercadopago.android.px.model.AmountConfiguration;
import com.mercadopago.android.px.model.Card;
import com.mercadopago.android.px.model.CardMetadata;
import com.mercadopago.android.px.model.CustomSearchItem;
import com.mercadopago.android.px.model.DiscountConfigurationModel;
import com.mercadopago.android.px.model.IPaymentDescriptor;
import com.mercadopago.android.px.model.PayerCost;
import com.mercadopago.android.px.model.PaymentData;
import com.mercadopago.android.px.model.PaymentMethod;
import com.mercadopago.android.px.model.PaymentMethods;
import com.mercadopago.android.px.model.PaymentTypes;
import com.mercadopago.android.px.model.exceptions.MercadoPagoError;
import com.mercadopago.android.px.model.internal.Application;
import com.mercadopago.android.px.model.internal.CheckoutResponse;
import com.mercadopago.android.px.model.internal.PaymentConfigurationData;
import com.mercadopago.android.px.model.internal.PaymentConfigurationMapper;
import com.mercadopago.android.px.model.internal.OneTapItem;
import com.mercadopago.android.px.model.internal.PaymentConfiguration;
import com.mercadopago.android.px.preferences.CheckoutPreference;
import java.util.Collections;
import com.mercadopago.android.px.tracking.internal.model.Reason;
import java.util.List;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.internal.matchers.apachecommons.ReflectionEquals;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PaymentServiceTest {

    private static final String CARD_ID_ESC_NOT_AVAILABLE = "113210124";

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Mock private UserSelectionRepository userSelectionRepository;
    @Mock private PaymentSettingRepository paymentSettingRepository;
    @Mock private DisabledPaymentMethodRepository disabledPaymentMethodRepository;
    @Mock private PaymentProcessor paymentProcessor;
    @Mock private Context context;
    @Mock private EscPaymentManager escPaymentManager;
    @Mock private ESCManagerBehaviour escManagerBehaviour;
    @Mock private AmountConfigurationRepository amountConfigurationRepository;
    @Mock private CongratsRepository congratsRepository;
    @Mock private PayerCostSelectionRepository payerCostSelectionRepository;
    @Mock private ApplicationSelectionRepository applicationSelectionRepository;
    @Mock private AdvancedConfiguration advancedConfiguration;
    @Mock private PostPaymentConfiguration postPaymentConfiguration;
    @Mock private com.mercadopago.android.px.configuration.PaymentConfiguration paymentConfiguration;

    @Mock private OneTapItem node;
    @Mock private PayerCost payerCost;
    @Mock private PaymentMethod paymentMethod;
    @Mock private Application application;
    @Mock private FileManager fileManager;
    @Mock private ValidationProgramUseCase validationProgramUseCase;
    @Mock private TokenizeWithEscUseCase tokenizeWithEscUseCase;
    @Mock private TokenizeWithoutCvvUseCase tokenizeWithoutCvvUseCase;
    @Mock private CustomOptionIdSolver customOptionIdSolver;
    @Mock private PaymentResultFactory paymentResultFactory;
    @Mock private PaymentDataFactory paymentDataFactory;

    private PaymentService paymentService;

    private static final DiscountConfigurationModel WITHOUT_DISCOUNT =
        new DiscountConfigurationModel(null, null, false);

    @Before
    public void setUp() {
        when(paymentSettingRepository.getAdvancedConfiguration()).thenReturn(advancedConfiguration);
        when(advancedConfiguration.getPostPaymentConfiguration()).thenReturn(postPaymentConfiguration);

        paymentService = new PaymentService(userSelectionRepository,
            paymentSettingRepository,
            disabledPaymentMethodRepository,
            context,
            escPaymentManager,
            escManagerBehaviour,
            amountConfigurationRepository,
            congratsRepository,
            fileManager,
            validationProgramUseCase,
            paymentResultFactory,
            paymentDataFactory
            fromPayerPaymentMethodToCardMapper,
            paymentMethodMapper,
            paymentMethodRepository,
            validationProgramUseCase,
            tokenizeWithEscUseCase,
            tokenizeWithoutCvvUseCase
        );

        application = mock(Application.class);
        final PaymentData paymentDataMock = mock(PaymentData.class);
        when(paymentDataMock.getPaymentMethod()).thenReturn(paymentMethod);
        when(paymentDataMock.getPayerCost()).thenReturn(payerCost);
        final Application.PaymentMethod applicationPaymentMethod = mock(Application.PaymentMethod.class);
        when(applicationPaymentMethod.getId()).thenReturn(PaymentMethods.ARGENTINA.AMEX);
        when(applicationPaymentMethod.getType()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(application.getPaymentMethod()).thenReturn(applicationPaymentMethod);
        when(paymentSettingRepository.getCheckoutPreference()).thenReturn(mock(CheckoutPreference.class));
        when(paymentSettingRepository.getPaymentConfiguration())
            .thenReturn(mock(com.mercadopago.android.px.configuration.PaymentConfiguration.class));
        when(PaymentConfigurationUtil.getPaymentProcessor(paymentSettingRepository.getPaymentConfiguration()))
            .thenReturn(paymentProcessor);
        when(paymentMethod.getId()).thenReturn(PaymentMethods.ARGENTINA.AMEX);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        when(paymentDataFactory.create()).thenReturn(Collections.singletonList(paymentDataMock));
        //noinspection unchecked
        when(paymentSettingRepository.getSecurityType()).thenReturn(SecurityType.SECOND_FACTOR);
    }

    private PaymentConfiguration mockPaymentConfiguration(@NonNull final OneTapItem oneTapItem,
        @Nullable final PayerCost payerCost) {
        final AmountConfiguration amountConfiguration = mock(AmountConfiguration.class);
        when(amountConfigurationRepository.getConfigurationSelectedFor(anyString())).thenReturn(amountConfiguration);
        when(amountConfiguration.getCurrentPayerCost(anyBoolean(), anyInt())).thenReturn(payerCost);
        when(applicationSelectionRepository.get(oneTapItem)).thenReturn(application);
        return new PaymentConfigurationMapper(amountConfigurationRepository,
            payerCostSelectionRepository, applicationSelectionRepository, customOptionIdSolver).map(
            new PaymentConfigurationData(oneTapItem));
    }

    @Test
    public void whenStorePayment() {
        paymentService.storePayment(mock(IPaymentDescriptor.class));

        verify(fileManager).writeToFile(any(), any(PaymentWrapper.class));
    }

    @Test
    public void whenRemoveStorePayment() {
        paymentService.reset();

        verify(fileManager).removeFile(any());
    }

    @Test
    public void whenOneTapPaymentIsCardSelectCard() {
        final Card card = creditCardPresetMock(CARD_ID_ESC_APPROVED);

        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(reflectionEquals(card), any());
    }

    @Test
    public void whenOneTapPaymentIsCardSelectPayerCost() {
        creditCardPresetMock(CARD_ID_ESC_APPROVED);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenOneTapPaymentIsCardPayerCostAndCardSet() {
        final Card card = creditCardPresetMock(CARD_ID_ESC_APPROVED);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenThrow(IllegalStateException.class);
        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(userSelectionRepository).select(reflectionEquals(card), any());
        verify(userSelectionRepository).select(payerCost);
    }

    @Test
    public void whenSavedCardAndESCSavedThenAskTokenButFailApiCallThenCVVIsRequiered() {
        final Observer<Pair<Card, Reason>> cvvRequiredObserver = mock(Observer.class);
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        paymentService.getObservableEvents().getRequireCvvLiveData().observeForever(cvvRequiredObserver);
        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);

        final ArgumentCaptor<Function1> successCaptor = ArgumentCaptor.forClass(Function1.class);
        final ArgumentCaptor<Function1> failureCaptor = ArgumentCaptor.forClass(Function1.class);
        verify(tokenizeWithEscUseCase).execute(eq(card), successCaptor.capture(), failureCaptor.capture());
        final MercadoPagoError mercadoPagoError = mock(MercadoPagoError.class);
        when(mercadoPagoError.getApiException()).thenReturn(mock(ApiException.class));
        failureCaptor.getValue().invoke(mercadoPagoError);
        verifyNoMoreInteractions(tokenizeWithEscUseCase);

        // if api call to tokenize fails, then ask for CVV.
        verify(cvvRequiredObserver).onChanged(any(Pair.class));
        verifyNoMoreInteractions(cvvRequiredObserver);
    }

    @Test
    public void whenOneTapPaymentWhenSavedCardAndESCSavedThenAskTokenSuccess() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);
        when(paymentSettingRepository.getSecurityType()).thenReturn(SecurityType.SECOND_FACTOR);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(mock(AmountConfiguration.class));

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        final ArgumentCaptor<Function1> successCaptor = ArgumentCaptor.forClass(Function1.class);
        final ArgumentCaptor<Function1> failureCaptor = ArgumentCaptor.forClass(Function1.class);
        verify(tokenizeWithEscUseCase).execute(eq(card), successCaptor.capture(), failureCaptor.capture());
        when(paymentSettingRepository.hasToken()).thenReturn(true);
        successCaptor.getValue().invoke(mock(Token.class));
        verifyNoMoreInteractions(tokenizeWithEscUseCase);
    }

    @Test
    public void whenOneTapPaymentWhenNotSavedCardAndESCSavedThenAskCVV() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_APPROVED);
        when(escPaymentManager.hasEsc(card)).thenReturn(false);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));

        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        verifyNoMoreInteractions(tokenizeWithEscUseCase);
    }

    @Test
    public void whenOneTapPaymentWhenCapExceededThenAskCVV() {
        final Observer<Pair<Card, Reason>> cvvRequiredObserver = mock(Observer.class);
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_REJECTED);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        paymentService.getObservableEvents().getRequireCvvLiveData().observeForever(cvvRequiredObserver);

        verify(escPaymentManager).hasEsc(card);
        verify(cvvRequiredObserver).onChanged(any(Pair.class));
        verifyNoMoreInteractions(cvvRequiredObserver);
        verifyNoMoreInteractions(escPaymentManager);
        verifyNoMoreInteractions(tokenizeWithEscUseCase);
    }

    @Test
    public void whenOneTapPaymentWhenCapNotExceededAndNotApproved() {
        final Card card = savedCreditCardOneTapPresent(CARD_ID_ESC_NOT_AVAILABLE);
        when(escPaymentManager.hasEsc(card)).thenReturn(true);
        when(escManagerBehaviour.isESCEnabled()).thenReturn(true);
        when(paymentSettingRepository.getSecurityType()).thenReturn(SecurityType.SECOND_FACTOR);
        when(amountConfigurationRepository.getCurrentConfiguration()).thenReturn(mock(AmountConfiguration.class));

        paymentService.startExpressPayment(mockPaymentConfiguration(node, payerCost));
        verify(escPaymentManager).hasEsc(card);
        verifyNoMoreInteractions(escPaymentManager);
        final ArgumentCaptor<Function1> successCaptor = ArgumentCaptor.forClass(Function1.class);
        final ArgumentCaptor<Function1> failureCaptor = ArgumentCaptor.forClass(Function1.class);
        verify(tokenizeWithEscUseCase).execute(eq(card), successCaptor.capture(), failureCaptor.capture());
        when(paymentSettingRepository.hasToken()).thenReturn(true);
        successCaptor.getValue().invoke(mock(Token.class));
        verifyNoMoreInteractions(tokenizeWithEscUseCase);
    }

    @Test
    public void whenOneTapStartPaymentAndPaymentError() {
        final Observer<MercadoPagoError> errorObserver = mock(Observer.class);
        final ArgumentCaptor<PaymentServiceHandler> paymentServiceHandlerCaptor =
            ArgumentCaptor.forClass(PaymentServiceHandler.class);
        final ArgumentCaptor<Function1> validationProgramSuccessCaptor =
            ArgumentCaptor.forClass(Function1.class);

        paymentService.startExpressPayment();
        paymentService.getObservableEvents().getPaymentErrorLiveData().observeForever(errorObserver);

        verify(validationProgramUseCase).execute(any(), validationProgramSuccessCaptor.capture());
        final Function1<String, Unit> mockExecute = validationProgramSuccessCaptor.getValue();
        mockExecute.invoke(null);
        verify(paymentProcessor).startPayment(any(), any(), paymentServiceHandlerCaptor.capture());

        final PaymentServiceHandler paymentServiceHandlerMock = paymentServiceHandlerCaptor.getValue();
        final MercadoPagoError error = MercadoPagoError.createNotRecoverable("error");
        paymentServiceHandlerMock.onPaymentError(error);
        verify(errorObserver).onChanged(error);
    }

    @Test
    public void whenOneTapStartPaymentAndShouldShowVisualPayment() {
        final Observer<Unit> visualPaymentObserver = mock(Observer.class);
        when(paymentProcessor.shouldShowFragmentOnPayment(any(CheckoutPreference.class))).thenReturn(true);

        paymentService.startExpressPayment();
        paymentService.getObservableEvents().getVisualPaymentLiveData().observeForever(visualPaymentObserver);

        verify(visualPaymentObserver).onChanged(any());
    }

    @Test
    public void whenOneTapPaymentWhenHasTokenAndPaymentSuccess() {
        final ArgumentCaptor<CheckoutData> checkoutDataCaptor =
            ArgumentCaptor.forClass(CheckoutData.class);

        final ArgumentCaptor<Function1> validationProgramSuccessCaptor =
            ArgumentCaptor.forClass(Function1.class);

        savedCreditCardOneTapPresent(CARD_ID_ESC_NOT_AVAILABLE);
        when(paymentProcessor.shouldShowFragmentOnPayment(any(CheckoutPreference.class))).thenReturn(false);

        final PaymentConfiguration configuration = mockPaymentConfiguration(node, payerCost);
        paymentService.startExpressPayment();
        verify(validationProgramUseCase).execute(any(), validationProgramSuccessCaptor.capture());
        final Function1<String, Unit> mockExecute = validationProgramSuccessCaptor.getValue();
        mockExecute.invoke(null);
        verify(paymentProcessor).startPayment(any(), checkoutDataCaptor.capture(), any());

        final PaymentMethod actualPm = checkoutDataCaptor.getValue().paymentDataList.get(0).getPaymentMethod();
        final PayerCost actualPc = checkoutDataCaptor.getValue().paymentDataList.get(0).getPayerCost();

        assertEquals(actualPm.getId(), configuration.component1());
        assertEquals(actualPm.getPaymentTypeId(), configuration.component2());
        assertTrue(new ReflectionEquals(actualPc).matches(payerCost));
    }

    @NonNull
    private Card savedCreditCardOneTapPresent(final String cardId) {
        final Card card = creditCardPresetMock(cardId);
        when(paymentMethod.getPaymentTypeId()).thenReturn(PaymentTypes.CREDIT_CARD);
        return card;
    }

    private Card getCardById(@NonNull final String cardId) {
        final CheckoutResponse checkoutResponse = CheckoutResponseStub.FULL.get();
        for (final CustomSearchItem customSearchItem : checkoutResponse.getPayerPaymentMethods()) {
            if (customSearchItem.getId().equals(cardId)) {
                final PaymentMethod paymentMethod = getPaymentMethodById(
                    checkoutResponse.getAvailablePaymentMethods(),
                    customSearchItem.getPaymentMethodId());
                final Card card = new Card();
                card.setId(cardId);
                card.setSecurityCode(paymentMethod != null ? paymentMethod.getSecurityCode() : null);
                card.setPaymentMethod(paymentMethod);
                card.setFirstSixDigits(customSearchItem.getFirstSixDigits());
                card.setLastFourDigits(customSearchItem.getLastFourDigits());
                card.setIssuer(customSearchItem.getIssuer());
                card.setEscStatus(customSearchItem.getEscStatus());
                return card;
            }
        }
        return null;
    }

    @Nullable
    private PaymentMethod getPaymentMethodById(
        @NonNull final List<PaymentMethod> paymentMethods,
        @Nullable final String paymentMethodId) {
        for (final PaymentMethod paymentMethod : paymentMethods) {
            if (paymentMethod.getId().equals(paymentMethodId)) {
                return paymentMethod;
            }
        }
        return null;
    }

    private Card creditCardPresetMock(final String cardId) {
        final Card card = getCardById(cardId);
        final CardMetadata cardMetadata = mock(CardMetadata.class);
        when(node.getCard()).thenReturn(cardMetadata);
        when(customOptionIdSolver.get(node)).thenReturn(cardId);
        return card;
    }
}