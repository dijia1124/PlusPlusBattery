package com.example.plusplusbattery

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

val DUAL_BATTERY_KEY = booleanPreferencesKey("dual_battery_key")

val MULTIPLY_KEY = booleanPreferencesKey("multiply_key")
val MULTIPLIER_MAGNITUDE_KEY = intPreferencesKey("multiplier_magnitude_key")
val ESTIMATED_FCC_KEY = intPreferencesKey("estimated_fcc")
val ROOT_MODE_KEY = booleanPreferencesKey("root_mode_enabled")
val SHOW_SWITCH_ON_DASHBOARD = booleanPreferencesKey("show_root_switch_dashboard")
val DARK_MODE_KEY = booleanPreferencesKey("dark_mode_enabled")
val FOLLOW_SYSTEM_THEME_KEY = booleanPreferencesKey("follow_system_theme")