package com.example.plusplusbattery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.combine
import kotlin.math.pow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val IS_DUAL_BATTERY = 2
private const val IS_SINGLE_BATTERY = 1

class BatteryInfoViewModel(application: Application, private val batteryInfoRepository: BatteryInfoRepository, private val historyInfoRepository: HistoryInfoRepository) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val dataStore = context.dataStore

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    val isRootMode: StateFlow<Boolean> = dataStore.data
        .map { prefs -> prefs[ROOT_MODE_KEY] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setRootMode(enabled: Boolean) = viewModelScope.launch {
        dataStore.edit { prefs -> prefs[ROOT_MODE_KEY] = enabled }
    }

    val showSwitchOnDashboard: StateFlow<Boolean> = dataStore.data
        .map { it[SHOW_SWITCH_ON_DASHBOARD] != false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setShowSwitchOnDashboard(show: Boolean) = viewModelScope.launch {
        dataStore.edit { it[SHOW_SWITCH_ON_DASHBOARD] = show }
    }

    val savedEstimatedFcc: StateFlow<String> = dataStore.data
        .map { it[ESTIMATED_FCC_KEY]?.toString() ?: context.getString(R.string.estimating_full_charge_capacity) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, context.getString(R.string.estimating_full_charge_capacity))

    val isDualBatt: StateFlow<Boolean> = dataStore.data
        .map { it[DUAL_BATTERY_KEY] == true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val dualBattMultiplier: StateFlow<Int> = isDualBatt
        .map { if (it) IS_DUAL_BATTERY else IS_SINGLE_BATTERY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, IS_SINGLE_BATTERY)

    fun setDualBat(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[DUAL_BATTERY_KEY] = enabled }
        }
    }

    val isMultiply: StateFlow<Boolean> = dataStore.data
        .map { it[MULTIPLY_KEY] != false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val selectedMagnitude: StateFlow<Int> = dataStore.data
        .map { it[MULTIPLIER_MAGNITUDE_KEY] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val calibMultiplier: StateFlow<Double> = combine(isMultiply, selectedMagnitude) { isMult, mag ->
        if (isMult) 10.0.pow(mag.toDouble()) else 1 / 10.0.pow(mag.toDouble())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 1.0)

    fun setMultiplierPrefs(isMultiply: Boolean, magnitude: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[MULTIPLY_KEY] = isMultiply
                it[MULTIPLIER_MAGNITUDE_KEY] = magnitude
            }
        }
    }

    suspend fun refreshBatteryInfo(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getBasicBatteryInfo()
        }

    suspend fun refreshBatteryInfoWithRoot(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getRootBatteryInfo(calibMultiplier.value, dualBattMultiplier.value)
        }

    suspend fun refreshNonRootVoltCurrPwr(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getNonRootVoltCurrPwr(calibMultiplier.value, dualBattMultiplier.value)
        }

    suspend fun refreshEstimatedFcc(): BatteryInfo =
        withContext(Dispatchers.IO) {
           batteryInfoRepository.getEstimatedFcc(savedEstimatedFcc.value)
        }

    suspend fun saveCycleCount(): Unit =
        withContext(Dispatchers.IO) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
            val date = System.currentTimeMillis()
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
            val newInfo = HistoryInfo(date = date, dateString = dateString, cycleCount = cycleCount.toString())
            historyInfoRepository.insertOrUpdate(newInfo)
    }
}

