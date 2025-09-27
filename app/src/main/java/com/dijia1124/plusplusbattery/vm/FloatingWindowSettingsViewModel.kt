package com.dijia1124.plusplusbattery.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FloatingWindowSettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepository = PrefsRepository(application)

    val floatingWindowAlpha = prefsRepository.floatingWindowAlpha.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = 0.75f
    )
    val floatingWindowSize = prefsRepository.floatingWindowSize.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = 1.0f
    )
    val floatingWindowTouchable = prefsRepository.floatingWindowTouchable.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = true
    )
    val floatingWindowTextColor = prefsRepository.floatingWindowTextColor.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = "auto"
    )
    val floatingWindowBackgroundColor = prefsRepository.floatingWindowBackgroundColor.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = "auto"
    )

    fun setFloatingWindowAlpha(alpha: Float) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowAlpha(alpha)
        }
    }

    fun setFloatingWindowSize(size: Float) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowSize(size)
        }
    }

    fun setFloatingWindowTouchable(touchable: Boolean) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowTouchable(touchable)
        }
    }

    fun setFloatingWindowTextColor(colorKey: String) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowTextColor(colorKey)
        }
    }

    fun setFloatingWindowBackgroundColor(colorKey: String) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowBackgroundColor(colorKey)
        }
    }

    fun resetFloatingWindowSettings() {
        setFloatingWindowAlpha(0.75f)
        setFloatingWindowSize(1.0f)
        setFloatingWindowTouchable(true)
        setFloatingWindowTextColor("auto")
        setFloatingWindowBackgroundColor("auto")
    }
}
