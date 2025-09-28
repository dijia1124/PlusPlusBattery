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
    val floatingWindowTextShadowEnabled = prefsRepository.floatingWindowTextShadowEnabled.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = false
    )

    val floatingWindowFontWeight = prefsRepository.floatingWindowFontWeight.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = 400
    )

    val floatingWindowTextStrokeEnabled = prefsRepository.floatingWindowTextStrokeEnabled.stateIn(
        viewModelScope, SharingStarted.Eagerly, initialValue = false
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

    fun setFloatingWindowTextShadowEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowTextShadowEnabled(enabled)
        }
    }

    fun setFloatingWindowFontWeight(fontWeight: Int) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowFontWeight(fontWeight)
        }
    }

    fun setFloatingWindowTextStrokeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setFloatingWindowTextStrokeEnabled(enabled)
        }
    }

    fun resetFloatingWindowSettings() {
        setFloatingWindowAlpha(0.75f)
        setFloatingWindowSize(1.0f)
        setFloatingWindowTouchable(true)
        setFloatingWindowTextColor("auto")
        setFloatingWindowBackgroundColor("auto")
        setFloatingWindowTextShadowEnabled(false)
        setFloatingWindowFontWeight(400)
        setFloatingWindowTextStrokeEnabled(false)
    }
}
