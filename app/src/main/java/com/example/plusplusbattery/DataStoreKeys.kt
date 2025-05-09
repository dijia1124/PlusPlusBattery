package com.example.plusplusbattery

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

val DUAL_BATTERY_KEY = booleanPreferencesKey("dual_battery_key")

val MULTIPLY_KEY = booleanPreferencesKey("multiply_key")
val MULTIPLIER_MAGNITUDE_KEY = intPreferencesKey("multiplier_magnitude_key")
val ESTIMATED_FCC_KEY = intPreferencesKey("estimated_fcc")
val ROOT_MODE_KEY = booleanPreferencesKey("root_mode_enabled")