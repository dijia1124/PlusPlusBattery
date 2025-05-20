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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BatteryInfoViewModel(application: Application, private val batteryInfoRepository: BatteryInfoRepository, private val prefsRepo: PrefsRepository, private val historyInfoRepository: HistoryInfoRepository) : AndroidViewModel(application) {
    private val context = application.applicationContext

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

    val isRootMode: StateFlow<Boolean> = prefsRepo.isRootModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setRootMode(enabled: Boolean) = viewModelScope.launch {
        prefsRepo.setRootMode(enabled)
    }

    val showSwitchOnDashboard: StateFlow<Boolean> = prefsRepo.showSwitchOnDashboardFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun setShowSwitchOnDashboard(show: Boolean) = viewModelScope.launch {
        prefsRepo.setShowSwitchOnDashboard(show)
    }

    val savedEstimatedFcc: StateFlow<String> = batteryInfoRepository.estimatedFccFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            getApplication<Application>().getString(R.string.estimating_full_charge_capacity)
        )

    val isDualBatt: StateFlow<Boolean> = batteryInfoRepository.isDualBattFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setDualBatt(enabled: Boolean) = viewModelScope.launch {
        batteryInfoRepository.setDualBatt(enabled)
    }

    val isMultiply: StateFlow<Boolean> = batteryInfoRepository.isMultiplyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val selectedMagnitude: StateFlow<Int> = batteryInfoRepository.selectedMagnitudeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setMultiplierPrefs(isMultiply: Boolean, magnitude: Int) {
        viewModelScope.launch {
            batteryInfoRepository.setMultiplierPrefs(isMultiply, magnitude)
        }
    }

    suspend fun refreshBatteryInfo(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getBasicBatteryInfo()
        }

    suspend fun refreshBatteryInfoWithRoot(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getRootBatteryInfo()
        }

    suspend fun refreshNonRootVoltCurrPwr(): List<BatteryInfo> =
        withContext(Dispatchers.IO) {
            batteryInfoRepository.getNonRootVoltCurrPwr()
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

