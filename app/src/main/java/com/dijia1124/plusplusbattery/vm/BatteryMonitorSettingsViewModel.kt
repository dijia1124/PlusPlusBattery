package com.dijia1124.plusplusbattery.vm

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.repository.BatteryInfoRepository
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import com.dijia1124.plusplusbattery.service.BatteryMonitorService
import com.dijia1124.plusplusbattery.service.FloatingWindowService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatteryMonitorSettingsViewModel(
    application: Application,
    private val batteryRepo: BatteryInfoRepository = BatteryInfoRepository(application),
    private val prefsRepo: PrefsRepository = PrefsRepository(application)
) : AndroidViewModel(application) {

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    private val _isFloatingWindowOn = MutableStateFlow(false)
    val isFloatingWindowOn: StateFlow<Boolean> = _isFloatingWindowOn.asStateFlow()
    private val appContext = getApplication<Application>()

    fun startMonitor() {
        Intent(appContext, BatteryMonitorService::class.java).also { intent ->
            ContextCompat.startForegroundService(appContext, intent)
        }
        _isMonitoring.value = true
    }

    fun stopMonitor() {
        Intent(appContext, BatteryMonitorService::class.java).also { intent ->
            appContext.stopService(intent)
        }
        _isMonitoring.value = false
    }

    fun startFloatingWindow() {
        Intent(appContext, FloatingWindowService::class.java).also { intent ->
            appContext.startService(intent)
        }
        _isFloatingWindowOn.value = true
    }

    fun stopFloatingWindow() {
        Intent(appContext, FloatingWindowService::class.java).also { intent ->
            appContext.stopService(intent)
        }
        _isFloatingWindowOn.value = false
    }


    // Holds the list of all available BatteryInfo titles for selection
    private val _availableEntries = MutableStateFlow<List<BatteryInfoType>>(emptyList())
    val availableEntries: StateFlow<List<BatteryInfoType>> = _availableEntries

    // Holds the set of user-selected titles (entries) from preferences
    val visibleEntries: StateFlow<Set<BatteryInfoType>> =
        prefsRepo.visibleEntriesFlow
            .stateIn(viewModelScope, SharingStarted.Companion.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            // Fetch non-root and root infos
            val basicInfos = batteryRepo.getBasicBatteryInfo()
            val nonRootInfos = batteryRepo.getNonRootVoltCurrPwr()
            val rootInfos = batteryRepo.getRootBatteryInfo()
            val customInfos = batteryRepo.readCustomEntries()

            // Combine and extract unique titles
            val allTitles = (basicInfos + nonRootInfos + rootInfos + customInfos)
                .map { it.type }
                .distinct()

            _availableEntries.value = allTitles
        }
    }

    // Update the set of visible entries in DataStore
    fun setVisibleEntries(newEntries: Set<BatteryInfoType>) {
        viewModelScope.launch {
            prefsRepo.setVisibleEntries(newEntries)
        }
    }
}