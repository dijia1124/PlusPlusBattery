package com.dijia1124.plusplusbattery.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.util.DAILY_HISTORY_ENABLED
import com.dijia1124.plusplusbattery.data.util.DARK_MODE_KEY
import com.dijia1124.plusplusbattery.data.util.FLOATING_WINDOW_ALPHA
import com.dijia1124.plusplusbattery.data.util.FLOATING_WINDOW_SIZE
import com.dijia1124.plusplusbattery.data.util.FLOATING_WINDOW_TOUCHABLE
import com.dijia1124.plusplusbattery.data.util.FOLLOW_SYSTEM_THEME_KEY
import com.dijia1124.plusplusbattery.data.util.MONITOR_VISIBLE_ENTRIES
import com.dijia1124.plusplusbattery.data.util.POWER_CHART_EXPANDED_KEY
import com.dijia1124.plusplusbattery.data.util.REFRESH_INTERVAL_KEY
import com.dijia1124.plusplusbattery.data.util.ROOT_MODE_KEY
import com.dijia1124.plusplusbattery.data.util.SHOW_OPLUS_FIELDS
import com.dijia1124.plusplusbattery.data.util.SHOW_SWITCH_ON_DASHBOARD
import com.dijia1124.plusplusbattery.data.util.dataStore
import com.dijia1124.plusplusbattery.service.DailyHistoryWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

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

    val dailyHistoryEnabled: Flow<Boolean> =
        dataStore.data.map { it[DAILY_HISTORY_ENABLED] ?: false }

    suspend fun setDailyHistoryEnabled(context: Context, enabled: Boolean) {
        dataStore.edit { it[DAILY_HISTORY_ENABLED] = enabled }

        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyHistoryWorker>(24, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                "DailyHistoryLog",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
        } else {
            workManager.cancelUniqueWork("DailyHistoryLog")
        }
    }

    val floatingWindowAlpha: Flow<Float> =
        dataStore.data.map { it[FLOATING_WINDOW_ALPHA] ?: 1.0f }

    suspend fun setFloatingWindowAlpha(alpha: Float) {
        dataStore.edit { it[FLOATING_WINDOW_ALPHA] = alpha }
    }

    val floatingWindowSize: Flow<Float> =
        dataStore.data.map { it[FLOATING_WINDOW_SIZE] ?: 1.0f }

    suspend fun setFloatingWindowSize(size: Float) {
        dataStore.edit { it[FLOATING_WINDOW_SIZE] = size }
    }

    val floatingWindowTouchable: Flow<Boolean> =
        dataStore.data.map { it[FLOATING_WINDOW_TOUCHABLE] ?: true }

    suspend fun setFloatingWindowTouchable(touchable: Boolean) {
        dataStore.edit { it[FLOATING_WINDOW_TOUCHABLE] = touchable }
    }
}