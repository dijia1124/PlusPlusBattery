package com.example.plusplusbattery

import android.content.Context
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrefsRepository(context: Context) {

    private val dataStore = context.dataStore

    val isRootModeFlow: Flow<Boolean> =
        dataStore.data.map { it[ROOT_MODE_KEY] == true }

    val showSwitchOnDashboardFlow: Flow<Boolean> =
        dataStore.data.map { it[SHOW_SWITCH_ON_DASHBOARD] != false }

    suspend fun setRootMode(enabled: Boolean) =
        dataStore.edit { it[ROOT_MODE_KEY] = enabled }

    suspend fun setShowSwitchOnDashboard(show: Boolean) =
        dataStore.edit { it[SHOW_SWITCH_ON_DASHBOARD] = show }

    val darkModeEnabled: Flow<Boolean> =
        dataStore.data.map { it[DARK_MODE_KEY] == true }

    val followSystemTheme: Flow<Boolean> =
        dataStore.data.map { it[FOLLOW_SYSTEM_THEME_KEY] != false }

    suspend fun setDarkMode(enabled: Boolean) =
        dataStore.edit { it[DARK_MODE_KEY] = enabled }

    suspend fun setFollowSystemTheme(enabled: Boolean) =
        dataStore.edit { it[FOLLOW_SYSTEM_THEME_KEY] = enabled }
}
