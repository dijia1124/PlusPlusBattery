package com.dijia1124.plusplusbattery.data.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

val DUAL_BATTERY_KEY = booleanPreferencesKey("dual_battery_key")

val MULTIPLY_KEY = booleanPreferencesKey("multiply_key")
val MULTIPLIER_MAGNITUDE_KEY = intPreferencesKey("multiplier_magnitude_key")
val ESTIMATED_FCC_KEY = intPreferencesKey("estimated_fcc")
val ROOT_MODE_KEY = booleanPreferencesKey("root_mode_enabled")
val SHOW_SWITCH_ON_DASHBOARD = booleanPreferencesKey("show_root_switch_dashboard")
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
val FOLLOW_SYSTEM_THEME_KEY = booleanPreferencesKey("follow_system_theme")
val MONITOR_VISIBLE_ENTRIES = stringPreferencesKey("monitor_visible_entries")
val REFRESH_INTERVAL_KEY = intPreferencesKey("refresh_rate")
val SHOW_OPLUS_FIELDS = booleanPreferencesKey("show_oplus_fields")
val CUSTOM_FIELDS = stringPreferencesKey("custom_fields")