package com.dijia1124.plusplusbattery.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.util.DARK_MODE_KEY
import com.dijia1124.plusplusbattery.data.util.FOLLOW_SYSTEM_THEME_KEY
import com.dijia1124.plusplusbattery.data.util.MONITOR_VISIBLE_ENTRIES
import com.dijia1124.plusplusbattery.data.util.POWER_CHART_EXPANDED_KEY
import com.dijia1124.plusplusbattery.data.util.REFRESH_INTERVAL_KEY
import com.dijia1124.plusplusbattery.data.util.ROOT_MODE_KEY
import com.dijia1124.plusplusbattery.data.util.SHOW_OPLUS_FIELDS
import com.dijia1124.plusplusbattery.data.util.SHOW_SWITCH_ON_DASHBOARD
import com.dijia1124.plusplusbattery.data.util.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrefsRepository(context: Context) {

    private val dataStore = context.dataStore

    private val defaultVisibleEntries = emptySet<BatteryInfoType>()

    val refreshInterval: Flow<Int> =
        dataStore.data.map { it[REFRESH_INTERVAL_KEY] ?: 1000 }

    suspend fun setRefreshInterval(rate: Int) {
        dataStore.edit { it[REFRESH_INTERVAL_KEY] = rate }
    }

    val isRootModeFlow: Flow<Boolean> =
        dataStore.data.map { it[ROOT_MODE_KEY] == true }

    val showSwitchOnDashboardFlow: Flow<Boolean> =
        dataStore.data.map { it[SHOW_SWITCH_ON_DASHBOARD] != false }

    suspend fun setRootMode(enabled: Boolean) =
        dataStore.edit { it[ROOT_MODE_KEY] = enabled }

    private val keyToType = BatteryInfoType.entries.associateBy { it.key }

    val visibleEntriesFlow: Flow<Set<BatteryInfoType>> = dataStore.data
        .map { prefs ->
            (prefs[MONITOR_VISIBLE_ENTRIES]
                ?.takeIf(String::isNotBlank)
                ?.split(',')
                ?.mapNotNull { raw -> keyToType[ raw.trim() ] }
                ?.toSet()
                ?: defaultVisibleEntries)
        }

    suspend fun setVisibleEntries(keys: Set<BatteryInfoType>) {
        dataStore.edit { prefs ->
            prefs[MONITOR_VISIBLE_ENTRIES] = keys.joinToString(",") { it.key }
        }
    }

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

    // show oplus fields by default
    val showOplusFields: Flow<Boolean> =
        dataStore.data.map { it[SHOW_OPLUS_FIELDS] != false }

    suspend fun setShowOplusFields(enabled: Boolean) =
        dataStore.edit { it[SHOW_OPLUS_FIELDS] = enabled }

    val isPowerChartExpanded: Flow<Boolean> =
        dataStore.data.map { it[POWER_CHART_EXPANDED_KEY] ?: false }

    suspend fun setPowerChartExpanded(expanded: Boolean) {
        dataStore.edit { it[POWER_CHART_EXPANDED_KEY] = expanded }
    }
}