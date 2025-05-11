package com.example.plusplusbattery

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = PrefsRepository(application)

    val darkModeEnabled = prefs.darkModeEnabled.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = false
    )
    val followSystemTheme = prefs.followSystemTheme.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = true
    )

    fun setDarkMode(enabled: Boolean) = viewModelScope.launch {
        prefs.setDarkMode(enabled)
    }

    fun setFollowSystem(enabled: Boolean) = viewModelScope.launch {
        prefs.setFollowSystemTheme(enabled)
        if (enabled) prefs.setDarkMode(false)
    }
}
