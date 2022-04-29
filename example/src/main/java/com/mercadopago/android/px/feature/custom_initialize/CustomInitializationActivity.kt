package com.mercadopago.android.px.feature.custom_initialize

import android.annotation.SuppressLint
import androidx.lifecycle.Observer
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.mercadopago.android.px.di.Dependencies
import com.mercadopago.example.R

class CustomInitializationActivity : AppCompatActivity() {

    private lateinit var viewModel: CustomInitializationViewModel
    private lateinit var localeInput: AutoCompleteTextView
    private lateinit var publicKeyInput: AutoCompleteTextView
    private lateinit var preferenceIdInput: AutoCompleteTextView
    private lateinit var accessTokenInput: AutoCompleteTextView
    private lateinit var oneTapCheck: CheckBox
    private lateinit var defaultProcessor: RadioButton
    private lateinit var visualProcessor: RadioButton
    private lateinit var noVisualProcessor: RadioButton
    private lateinit var clearButton: Button
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_initialization)
        viewModel = Dependencies.instance.viewModelModule!!.get(this, CustomInitializationViewModel::class.java)
        localeInput = findViewById(R.id.localeInput)
        publicKeyInput = findViewById(R.id.publicKeyInput)
        preferenceIdInput = findViewById(R.id.preferenceIdInput)
        accessTokenInput = findViewById(R.id.accessTokenInput)
        oneTapCheck = findViewById(R.id.one_tap)
        defaultProcessor = findViewById(R.id.default_processor)
        visualProcessor = findViewById(R.id.visual_processor)
        noVisualProcessor = findViewById(R.id.no_visual_processor)
        clearButton = findViewById(R.id.clearButton)
        startButton = findViewById(R.id.startButton)
        configureViews()
        bindViewModel(savedInstanceState)
        viewModel.initialize()
    }

    override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        viewModel.storeInBundle(bundle)
    }

    private fun bindViewModel(savedInstanceState: Bundle?) {
        savedInstanceState?.let { viewModel.recoverFromBundle(savedInstanceState) }
        viewModel.uiStateLiveData.observe(this, Observer { state ->
            when (state) {
                is CustomInitializeState.LoadData -> {
                    loadData(state.initializationData)
                }
                is CustomInitializeState.InitCheckout -> {
                    state.builder.build().startPayment(this, REQUEST_CODE)
                }
                else -> {
                }
            }
        })
    }

    private fun loadData(initializationData: InitializationData) {
        localeInput.setText(initializationData.locale.value)
        publicKeyInput.setText(initializationData.publicKey.value)
        preferenceIdInput.setText(initializationData.preferenceId.value)
        accessTokenInput.setText(initializationData.accessToken.value)
        oneTapCheck.isChecked = initializationData.oneTap.value
        defaultProcessor.isChecked = initializationData.processor.value == ProcessorType.DEFAULT
        visualProcessor.isChecked = initializationData.processor.value == ProcessorType.VISUAL
        noVisualProcessor.isChecked = initializationData.processor.value == ProcessorType.NO_VISUAL
    }

    private fun configureViews() {
        configInputDropDownView(localeInput, resources.getStringArray(R.array.locales))
        { value -> value?.let { viewModel.onConfigurationChanged(ConfigurationStringData.Locale(it)) } }
        configInputDropDownView(publicKeyInput, resources.getStringArray(R.array.public_key))
        { value -> value?.let { viewModel.onConfigurationChanged(ConfigurationStringData.PublicKey(it)) } }
        configInputDropDownView(preferenceIdInput, resources.getStringArray(R.array.preference_ids))
        { value -> value?.let { viewModel.onConfigurationChanged(ConfigurationStringData.PreferenceId(it)) } }
        configInputDropDownView(accessTokenInput, resources.getStringArray(R.array.access_tokens))
        { value -> value?.let { viewModel.onConfigurationChanged(ConfigurationStringData.AccessToken(it)) } }
        oneTapCheck.setOnCheckedChangeListener()
        { _, isChecked -> viewModel.onConfigurationChanged(ConfigurationBooleanData.OneTap(isChecked)) }
        defaultProcessor.setOnClickListener()
        {_ ->  viewModel.onConfigurationChanged(ConfigurationDataType.Processor(ProcessorType.DEFAULT))}
        visualProcessor.setOnClickListener()
        {_ ->  viewModel.onConfigurationChanged(ConfigurationDataType.Processor(ProcessorType.VISUAL))}
        noVisualProcessor.setOnClickListener()
        {_ ->  viewModel.onConfigurationChanged(ConfigurationDataType.Processor(ProcessorType.NO_VISUAL))}

        clearButton.setOnClickListener { viewModel.onClear() }
        startButton.setOnClickListener { viewModel.onStartButtonClicked() }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun configInputDropDownView(view: AutoCompleteTextView, items: Array<String>, updateValue: ((String?) -> Unit)) {
        ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items).also { adapter ->
            view.setAdapter(adapter)
        }
        view.setOnTouchListener { _, _ ->
            view.showDropDown()
            false
        }
        view.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateValue(s?.toString())
            }
        })
    }

    companion object {
        private const val REQUEST_CODE = 0x01
    }
}