package com.example.plusplusbattery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BatteryMonitorSettingsViewModel(
    application: Application,
    private val batteryRepo: BatteryInfoRepository = BatteryInfoRepository(application),
    private val prefsRepo: PrefsRepository = PrefsRepository(application)
) : AndroidViewModel(application) {

    // Holds the list of all available BatteryInfo titles for selection
    private val _availableEntries = MutableStateFlow<List<String>>(emptyList())
    val availableEntries: StateFlow<List<String>> = _availableEntries

    // Holds the set of user-selected titles (entries) from preferences
    val visibleEntries: StateFlow<Set<String>> =
        prefsRepo.visibleEntriesFlow
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    init {
        viewModelScope.launch {
            // Fetch non-root and root infos
            val basicInfos = batteryRepo.getBasicBatteryInfo()
            val nonRootInfos = batteryRepo.getNonRootVoltCurrPwr()
            val rootInfos = batteryRepo.getRootBatteryInfo()

            // Combine and extract unique titles
            val allTitles = (basicInfos + nonRootInfos + rootInfos)
                .map { it.title }
                .distinct()

            _availableEntries.value = allTitles
        }
    }

    // Update the set of visible entries in DataStore
    fun setVisibleEntries(newEntries: Set<String>) {
        viewModelScope.launch {
            prefsRepo.setVisibleEntries(newEntries)
        }
    }
}
