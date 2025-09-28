package com.dijia1124.plusplusbattery

import android.app.Application
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dijia1124.plusplusbattery.data.repository.PrefsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var prefsRepo: PrefsRepository

    var useDarkTheme by mutableStateOf(false)
        private set

    override fun onCreate() {
        super.onCreate()
        prefsRepo = PrefsRepository(this)
        applicationScope.launch {
            // Combine the two flows, so that any change to either will trigger an update
            prefsRepo.followSystemTheme.combine(prefsRepo.darkModeEnabled) { follow, dark ->
                Pair(follow, dark)
            }.collect { (follow, dark) ->
                updateTheme(follow, dark)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-read the settings from DataStore when config changes
        applicationScope.launch {
            val followSystem = prefsRepo.followSystemTheme.first()
            val darkMode = prefsRepo.darkModeEnabled.first()
            updateTheme(followSystem, darkMode)
        }
    }

    private fun updateTheme(followSystem: Boolean, darkMode: Boolean) {
        val isSysDark = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        useDarkTheme = if (followSystem) isSysDark else darkMode
    }
}