package com.dijia1124.plusplusbattery.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsRepository(application)

    private val _hasRoot = MutableStateFlow(false)
    val hasRoot: StateFlow<Boolean> = _hasRoot

    init {
        // check if the device has root access
        viewModelScope.launch {
            _hasRoot.value = try {
                hasRootAccess()
            } catch (e: Exception) {
                false
            }
        }
    }

    private suspend fun hasRootAccess(): Boolean = withContext(Dispatchers.IO) {
        try {
            Shell.cmd("su -c whoami").exec().isSuccess
        } catch (e: Exception) {
            false
        }
    }
    val refreshInterval = prefs.refreshInterval.stateIn(
        viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), initialValue = 1000
    )

    val darkModeEnabled = prefs.darkModeEnabled.stateIn(
        viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), initialValue = false
    )
    val followSystemTheme = prefs.followSystemTheme.stateIn(
        viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), initialValue = true
    )

    fun setRefreshInterval(rate: Int) = viewModelScope.launch {
        prefs.setRefreshInterval(rate)
    }

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setDarkMode(enabled)
    }

    fun setFollowSystem(enabled: Boolean) = viewModelScope.launch {
        prefs.setFollowSystemTheme(enabled)
        if (enabled) prefs.setDarkMode(false)
    }
}