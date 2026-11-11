package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.common.engagement.EngagementTrafficController
import com.common.engagement.PromptType
import com.common.engagement.UserEngagementManager
import com.example.data.local.ConversionHistoryEntity
import com.example.data.local.FavoritePairEntity
import com.example.data.local.FavoriteUnitPairEntity
import com.example.data.local.UnitConversionHistoryEntity
import com.example.data.model.BillSplitResult
import com.example.data.model.Currency
import com.example.data.model.HistoricalRatePoint
import com.example.data.model.PhysicalUnit
import com.example.data.model.UnitCategory
import com.example.data.repository.CurrencyRepository
import com.example.ui.calculator.ExpressionEvaluator
import com.example.ui.calculator.ExpressionEvaluator.toCleanString
import com.example.ui.security.BiometricAuthManager
import com.example.ui.security.BiometricStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CoinBridgeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CurrencyRepository(application)

    // Base & Targets
    private val _baseCurrency = MutableStateFlow(Currency.DEFAULT_BASE)
    val baseCurrency: StateFlow<Currency> = _baseCurrency.asStateFlow()

    private val _targetCurrencies = MutableStateFlow(Currency.DEFAULT_TARGETS)
    val targetCurrencies: StateFlow<List<Currency>> = _targetCurrencies.asStateFlow()

    // Calculator Expression & Amount
    private val _expressionInput = MutableStateFlow("100")
    val expressionInput: StateFlow<String> = _expressionInput.asStateFlow()

    private val _evaluatedAmount = MutableStateFlow(100.0)
    val evaluatedAmount: StateFlow<Double> = _evaluatedAmount.asStateFlow()

    // Rates State
    private val _currentRates = MutableStateFlow<Map<String, Double>>(emptyMap())
    val currentRates: StateFlow<Map<String, Double>> = _currentRates.asStateFlow()

    private val _convertedValues = MutableStateFlow<Map<String, Double>>(emptyMap())
    val convertedValues: StateFlow<Map<String, Double>> = _convertedValues.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Biometric Security State
    private val _isBiometricLockEnabled = MutableStateFlow(BiometricAuthManager.isBiometricLockEnabled(application))
    val isBiometricLockEnabled: StateFlow<Boolean> = _isBiometricLockEnabled.asStateFlow()

    private val _isAppLocked = MutableStateFlow(_isBiometricLockEnabled.value)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    private val _biometricStatus = MutableStateFlow(BiometricAuthManager.checkBiometricStatus(application))
    val biometricStatus: StateFlow<BiometricStatus> = _biometricStatus.asStateFlow()

    // Bill Splitter State
    private val _billAmountInput = MutableStateFlow("45.00")
    val billAmountInput: StateFlow<String> = _billAmountInput.asStateFlow()

    private val _billTipPercentage = MutableStateFlow(15f)
    val billTipPercentage: StateFlow<Float> = _billTipPercentage.asStateFlow()

    private val _billSplitCount = MutableStateFlow(2)
    val billSplitCount: StateFlow<Int> = _billSplitCount.asStateFlow()

    private val _billForeignCurrency = MutableStateFlow(Currency.ALL_CURRENCIES.find { it.code == "EUR" } ?: Currency.DEFAULT_BASE)
    val billForeignCurrency: StateFlow<Currency> = _billForeignCurrency.asStateFlow()

    private val _billHomeCurrency = MutableStateFlow(Currency.DEFAULT_BASE)
    val billHomeCurrency: StateFlow<Currency> = _billHomeCurrency.asStateFlow()

    private val _billSplitResult = MutableStateFlow<BillSplitResult?>(null)
    val billSplitResult: StateFlow<BillSplitResult?> = _billSplitResult.asStateFlow()

    // Trends Chart State
    private val _trendFromCurrency = MutableStateFlow(Currency.DEFAULT_BASE)
    val trendFromCurrency: StateFlow<Currency> = _trendFromCurrency.asStateFlow()

    private val _trendToCurrency = MutableStateFlow(Currency.ALL_CURRENCIES.find { it.code == "EUR" } ?: Currency.DEFAULT_BASE)
    val trendToCurrency: StateFlow<Currency> = _trendToCurrency.asStateFlow()

    private val _trendDaysRange = MutableStateFlow(30)
    val trendDaysRange: StateFlow<Int> = _trendDaysRange.asStateFlow()

    private val _trendPoints = MutableStateFlow<List<HistoricalRatePoint>>(emptyMap<String, Double>().map { HistoricalRatePoint("", 0.0) })
    val trendPoints: StateFlow<List<HistoricalRatePoint>> = _trendPoints.asStateFlow()

    private val _trendIsLoading = MutableStateFlow(false)
    val trendIsLoading: StateFlow<Boolean> = _trendIsLoading.asStateFlow()

    // Unit Converter State
    private val _unitCategory = MutableStateFlow(UnitCategory.WEIGHT)
    val unitCategory: StateFlow<UnitCategory> = _unitCategory.asStateFlow()

    private val _unitFrom = MutableStateFlow(PhysicalUnit.DEFAULT_WEIGHT_FROM)
    val unitFrom: StateFlow<PhysicalUnit> = _unitFrom.asStateFlow()

    private val _unitTo = MutableStateFlow(PhysicalUnit.DEFAULT_WEIGHT_TO)
    val unitTo: StateFlow<PhysicalUnit> = _unitTo.asStateFlow()

    private val _unitExpressionInput = MutableStateFlow("1")
    val unitExpressionInput: StateFlow<String> = _unitExpressionInput.asStateFlow()

    private val _unitEvaluatedAmount = MutableStateFlow(1.0)
    val unitEvaluatedAmount: StateFlow<Double> = _unitEvaluatedAmount.asStateFlow()

    private val _unitConvertedValue = MutableStateFlow(PhysicalUnit.convert(1.0, PhysicalUnit.DEFAULT_WEIGHT_FROM, PhysicalUnit.DEFAULT_WEIGHT_TO))
    val unitConvertedValue: StateFlow<Double> = _unitConvertedValue.asStateFlow()

    private val _unitEquivalents = MutableStateFlow<Map<String, Double>>(emptyMap())
    val unitEquivalents: StateFlow<Map<String, Double>> = _unitEquivalents.asStateFlow()

    // Shake Gesture State
    private val shakePrefs = application.getSharedPreferences("coinbridge_shake_prefs", Context.MODE_PRIVATE)
    private val _isShakeEnabled = MutableStateFlow(shakePrefs.getBoolean("shake_enabled", true))
    val isShakeEnabled: StateFlow<Boolean> = _isShakeEnabled.asStateFlow()

    private val _shakeThresholdGForce = MutableStateFlow(shakePrefs.getFloat("shake_threshold_g", 2.5f))
    val shakeThresholdGForce: StateFlow<Float> = _shakeThresholdGForce.asStateFlow()

    // Engagement Traffic Controller & Manager
    val trafficController = EngagementTrafficController(application)
    val userEngagementManager = UserEngagementManager.getInstance(application)
    private val _activeEngagementPrompt = MutableStateFlow(PromptType.NONE)
    val activeEngagementPrompt: StateFlow<PromptType> = _activeEngagementPrompt.asStateFlow()

    // Database Flows
    val favoritePairs: StateFlow<List<FavoritePairEntity>> = repository.getFavoritePairs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversionHistory: StateFlow<List<ConversionHistoryEntity>> = repository.getConversionHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unitConversionHistory: StateFlow<List<UnitConversionHistoryEntity>> = repository.getUnitHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteUnits: StateFlow<List<FavoriteUnitPairEntity>> = repository.getFavoriteUnits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var refreshJob: Job? = null

    init {
        refreshRates()
        recalculateBill()
        loadTrends()
        recalculateUnitConversions()
        refreshBiometricStatus()
        trafficController.recordAppLaunch()
        userEngagementManager.recordAppLaunch()
        com.example.ui.donation.DonationManager.init(application)
        com.example.ui.donation.MilestoneTracker.init(application)
        com.example.ui.donation.MilestoneTracker.recordAppLaunch(application)
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        BiometricAuthManager.setBiometricLockEnabled(getApplication(), enabled)
        _isBiometricLockEnabled.value = enabled
        if (!enabled) {
            _isAppLocked.value = false
        }
    }

    fun lockApp() {
        if (_isBiometricLockEnabled.value) {
            _isAppLocked.value = true
        }
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun refreshBiometricStatus() {
        _biometricStatus.value = BiometricAuthManager.checkBiometricStatus(getApplication())
    }

    fun refreshRates() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            _isLoading.value = true
            val (rates, offline) = repository.fetchLatestRates(_baseCurrency.value.code)
            _currentRates.value = rates
            _isOffline.value = offline
            _lastSyncTimestamp.value = repository.lastSyncTimestamp
            _isLoading.value = false

            recalculateConversions()
            recalculateBill()
            userEngagementManager.recordManualRefresh()
            evaluateEngagementPrompt(isCriticalFlowActive = false)
        }
    }

    fun onKeyInput(key: String) {
        val updated = ExpressionEvaluator.onKeyPress(_expressionInput.value, key)
        _expressionInput.value = updated
        _evaluatedAmount.value = ExpressionEvaluator.evaluate(updated)
        recalculateConversions()
        if (key == "=") {
            com.example.ui.donation.DonationManager.recordOperationSuccess(getApplication())
            com.example.ui.donation.MilestoneTracker.recordConversionSuccess(getApplication())
            trafficController.recordSuccessfulTask()
            userEngagementManager.recordSuccessfulOperation()
            evaluateEngagementPrompt(isCriticalFlowActive = false)
        }
    }

    fun setBaseCurrency(currency: Currency) {
        if (_baseCurrency.value.code == currency.code) return
        _baseCurrency.value = currency

        // If target list contains the new base, remove it or swap
        val newTargets = _targetCurrencies.value.filter { it.code != currency.code }
        _targetCurrencies.value = newTargets

        refreshRates()
    }

    fun addTargetCurrency(currency: Currency) {
        if (_targetCurrencies.value.any { it.code == currency.code } || currency.code == _baseCurrency.value.code) return
        _targetCurrencies.value = _targetCurrencies.value + currency
        recalculateConversions()
    }

    fun removeTargetCurrency(currency: Currency) {
        if (_targetCurrencies.value.size <= 1) return // Keep at least 1 target
        _targetCurrencies.value = _targetCurrencies.value.filter { it.code != currency.code }
        recalculateConversions()
    }

    fun swapBaseWithTarget(target: Currency) {
        val oldBase = _baseCurrency.value
        _baseCurrency.value = target
        _targetCurrencies.value = _targetCurrencies.value.map {
            if (it.code == target.code) oldBase else it
        }
        refreshRates()
    }

    private fun recalculateConversions() {
        viewModelScope.launch {
            val amount = _evaluatedAmount.value
            val rates = _currentRates.value
            val fromCode = _baseCurrency.value.code

            val map = mutableMapOf<String, Double>()
            _targetCurrencies.value.forEach { target ->
                val converted = repository.convertAmount(amount, fromCode, target.code, rates)
                map[target.code] = converted
            }
            _convertedValues.value = map

            // Log conversion if valid positive amount
            if (amount > 0.0 && _targetCurrencies.value.isNotEmpty()) {
                val firstTarget = _targetCurrencies.value.first()
                val converted = map[firstTarget.code] ?: 0.0
                repository.logConversion(fromCode, firstTarget.code, amount, converted)
            }
        }
    }

    // Bill Splitter Functions
    fun setBillAmount(input: String) {
        _billAmountInput.value = input
        recalculateBill()
    }

    fun setBillTipPercentage(percentage: Float) {
        _billTipPercentage.value = percentage
        recalculateBill()
    }

    fun setBillSplitCount(count: Int) {
        _billSplitCount.value = count.coerceIn(1, 20)
        recalculateBill()
    }

    fun setBillForeignCurrency(currency: Currency) {
        _billForeignCurrency.value = currency
        recalculateBill()
    }

    fun setBillHomeCurrency(currency: Currency) {
        _billHomeCurrency.value = currency
        recalculateBill()
    }

    private fun recalculateBill() {
        viewModelScope.launch {
            val raw = _billAmountInput.value.toDoubleOrNull() ?: 0.0
            val tipPct = _billTipPercentage.value
            val count = _billSplitCount.value
            val foreign = _billForeignCurrency.value
            val home = _billHomeCurrency.value

            val tipAmount = raw * (tipPct / 100.0)
            val totalBill = raw + tipAmount
            val perPersonRaw = if (count > 0) totalBill / count else totalBill

            // Convert per person to home currency
            val rate = repository.convertAmount(1.0, foreign.code, home.code, _currentRates.value)
            val perPersonConverted = perPersonRaw * rate

            _billSplitResult.value = BillSplitResult(
                rawAmount = raw,
                tipPercentage = tipPct,
                tipAmount = tipAmount,
                totalBillAmount = totalBill,
                splitCount = count,
                perPersonOriginal = perPersonRaw,
                perPersonConverted = perPersonConverted,
                fromCurrency = foreign,
                toCurrency = home,
                conversionRate = rate
            )
        }
    }

    // Trends Chart Functions
    fun setTrendPair(from: Currency, to: Currency) {
        _trendFromCurrency.value = from
        _trendToCurrency.value = to
        loadTrends()
    }

    fun setTrendDaysRange(days: Int) {
        _trendDaysRange.value = days
        loadTrends()
    }

    fun loadTrends() {
        viewModelScope.launch {
            _trendIsLoading.value = true
            val points = repository.getHistoricalTrends(
                fromCode = _trendFromCurrency.value.code,
                toCode = _trendToCurrency.value.code,
                days = _trendDaysRange.value
            )
            _trendPoints.value = points
            _trendIsLoading.value = false
        }
    }

    // Favorites Management
    fun toggleFavoritePair(fromCode: String, toCode: String) {
        viewModelScope.launch {
            val isFav = favoritePairs.value.any { it.fromCode == fromCode && it.toCode == toCode }
            if (isFav) {
                repository.removeFavorite(fromCode, toCode)
            } else {
                repository.addFavorite(fromCode, toCode)
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Unit Conversion Functions
    fun setUnitCategory(category: UnitCategory) {
        if (_unitCategory.value == category) return
        _unitCategory.value = category
        when (category) {
            UnitCategory.WEIGHT -> {
                _unitFrom.value = PhysicalUnit.DEFAULT_WEIGHT_FROM
                _unitTo.value = PhysicalUnit.DEFAULT_WEIGHT_TO
            }
            UnitCategory.LENGTH -> {
                _unitFrom.value = PhysicalUnit.DEFAULT_LENGTH_FROM
                _unitTo.value = PhysicalUnit.DEFAULT_LENGTH_TO
            }
        }
        recalculateUnitConversions()
    }

    fun setUnitFrom(unit: PhysicalUnit) {
        _unitFrom.value = unit
        recalculateUnitConversions()
    }

    fun setUnitTo(unit: PhysicalUnit) {
        _unitTo.value = unit
        recalculateUnitConversions()
    }

    fun swapUnits() {
        val temp = _unitFrom.value
        _unitFrom.value = _unitTo.value
        _unitTo.value = temp
        recalculateUnitConversions()
    }

    fun onUnitKeyInput(key: String) {
        val updated = ExpressionEvaluator.onKeyPress(_unitExpressionInput.value, key)
        _unitExpressionInput.value = updated
        _unitEvaluatedAmount.value = ExpressionEvaluator.evaluate(updated)
        recalculateUnitConversions()
        if (key == "=") {
            com.example.ui.donation.DonationManager.recordOperationSuccess(getApplication())
            com.example.ui.donation.MilestoneTracker.recordConversionSuccess(getApplication())
            trafficController.recordSuccessfulTask()
            userEngagementManager.recordSuccessfulOperation()
            evaluateEngagementPrompt(isCriticalFlowActive = false)
            logUnitConversion()
        }
    }

    var activeTab: String = "CONVERTER"

    fun onHardwareKeyInput(key: String) {
        when (activeTab) {
            "CONVERTER" -> onKeyInput(key)
            "UNIT" -> onUnitKeyInput(key)
            else -> onKeyInput(key)
        }
    }

    fun recalculateUnitConversions() {
        val amount = _unitEvaluatedAmount.value
        val from = _unitFrom.value
        val to = _unitTo.value
        _unitConvertedValue.value = PhysicalUnit.convert(amount, from, to)

        val categoryUnits = PhysicalUnit.getUnitsForCategory(_unitCategory.value)
        val matrix = mutableMapOf<String, Double>()
        categoryUnits.forEach { unit ->
            matrix[unit.id] = PhysicalUnit.convert(amount, from, unit)
        }
        _unitEquivalents.value = matrix
    }

    fun logUnitConversion() {
        viewModelScope.launch {
            val amount = _unitEvaluatedAmount.value
            if (amount > 0.0) {
                repository.logUnitConversion(
                    category = _unitCategory.value.name,
                    fromUnitId = _unitFrom.value.id,
                    toUnitId = _unitTo.value.id,
                    fromValue = amount,
                    toValue = _unitConvertedValue.value
                )
                userEngagementManager.recordSuccessfulConversion()
                trafficController.recordSuccessfulTask()
                evaluateEngagementPrompt(isCriticalFlowActive = false)
            }
        }
    }

    fun toggleFavoriteUnit(fromUnitId: String, toUnitId: String) {
        viewModelScope.launch {
            val isFav = favoriteUnits.value.any { it.fromUnitId == fromUnitId && it.toUnitId == toUnitId }
            if (isFav) {
                repository.removeFavoriteUnit(fromUnitId, toUnitId)
            } else {
                repository.toggleFavoriteUnit(_unitCategory.value.name, fromUnitId, toUnitId)
            }
        }
    }

    fun clearUnitHistory() {
        viewModelScope.launch {
            repository.clearUnitHistory()
        }
    }

    // Shake Gesture Handlers
    fun setShakeEnabled(enabled: Boolean) {
        shakePrefs.edit().putBoolean("shake_enabled", enabled).apply()
        _isShakeEnabled.value = enabled
    }

    fun setShakeThresholdGForce(threshold: Float) {
        shakePrefs.edit().putFloat("shake_threshold_g", threshold).apply()
        _shakeThresholdGForce.value = threshold
    }

    fun onDeviceShaken() {
        val firstTarget = _targetCurrencies.value.firstOrNull()
        if (firstTarget != null) {
            swapBaseWithTarget(firstTarget)
        }
    }

    // Engagement Traffic Controller Actions
    fun evaluateEngagementPrompt(isCriticalFlowActive: Boolean = false) {
        val prompt = userEngagementManager.checkPromptEligibility(isCriticalFlowActive = isCriticalFlowActive)
            .let { if (it == PromptType.NONE) trafficController.evaluateNextEligiblePrompt(isCriticalFlowActive = isCriticalFlowActive) else it }
        if (prompt != PromptType.NONE) {
            _activeEngagementPrompt.value = prompt
            userEngagementManager.recordPromptShown(prompt)
            trafficController.recordPromptShown(prompt)
        }
    }

    fun triggerManualRating() {
        _activeEngagementPrompt.value = PromptType.RATE_APP
    }

    fun triggerManualShare() {
        _activeEngagementPrompt.value = PromptType.SHARE_APP
    }

    fun dismissEngagementPrompt() {
        _activeEngagementPrompt.value = PromptType.NONE
    }

    fun markRatingCompleted() {
        userEngagementManager.markRatingCompleted()
        trafficController.markRatingCompleted()
        _activeEngagementPrompt.value = PromptType.NONE
    }

    fun markShareCompleted() {
        userEngagementManager.markShareCompleted()
        trafficController.markShareCompleted()
        _activeEngagementPrompt.value = PromptType.NONE
    }
}
