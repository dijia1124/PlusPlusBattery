package com.dijia1124.plusplusbattery.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dijia1124.plusplusbattery.data.repository.BatteryLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BatteryLogViewModel(
    application: Application,
    private val repo: BatteryLogRepository = BatteryLogRepository(),

    private val refreshIntervalMs: Long = 1000L
) : AndroidViewModel(application) {

    private val _latestLog = MutableStateFlow<Map<String, String>?>(null)
    val latestLog: StateFlow<Map<String, String>?> = _latestLog

    init {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val parsed = repo.getParsedLogcatData()
                    _latestLog.value = parsed
                } catch (t: Throwable) {
                    Log.e("BatteryLogVM", "logcat error", t)
                }
                delay(refreshIntervalMs)
            }
        }
    }
}