package com.dijia1124.plusplusbattery.data.util

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
val CUSTOM_ENTRIES = stringPreferencesKey("custom_entries")
val POWER_CHART_EXPANDED_KEY = booleanPreferencesKey("power_chart_expanded")
val DAILY_HISTORY_ENABLED = booleanPreferencesKey("daily_history_enabled")
val FLOATING_WINDOW_ALPHA = floatPreferencesKey("floating_window_alpha")
val FLOATING_WINDOW_SIZE = floatPreferencesKey("floating_window_size")
val FLOATING_WINDOW_TOUCHABLE = booleanPreferencesKey("floating_window_touchable")
val FLOATING_WINDOW_TEXT_COLOR = stringPreferencesKey("floating_window_text_color")
val FLOATING_WINDOW_BACKGROUND_COLOR = stringPreferencesKey("floating_window_background_color")
val FLOATING_WINDOW_TEXT_SHADOW = booleanPreferencesKey("floating_window_text_shadow")
val FLOATING_WINDOW_FONT_WEIGHT = intPreferencesKey("floating_window_font_weight")