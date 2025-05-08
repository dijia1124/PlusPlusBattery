package com.example.plusplusbattery

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.pow

private const val BCC_VOLTAGE_0_INDEX = 6
private const val BCC_VOLTAGE_1_INDEX = 11
private const val BCC_CURRENT_INDEX = 8
private const val IS_DUAL_BATTERY = 2
private const val IS_SINGLE_BATTERY = 1

class BatteryInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val dataStore = context.dataStore
    private val _batteryInfoList = MutableStateFlow<List<BatteryInfo>>(emptyList())
    private val _batteryInfoList2 = MutableStateFlow<List<BatteryInfo>>(emptyList())
    private val _batteryInfoList3 = MutableStateFlow<List<BatteryInfo>>(emptyList())

    private val refreshLock = Mutex()

    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val batteryInfoList: StateFlow<List<BatteryInfo>> = _batteryInfoList
    val batteryInfoList2: StateFlow<List<BatteryInfo>> = _batteryInfoList2
    val batteryInfoList3: StateFlow<List<BatteryInfo>> = _batteryInfoList3

    val isDualBatt: StateFlow<Boolean> = dataStore.data
        .map { it[DUAL_BATTERY_KEY] == true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val dualBattMultiplier: StateFlow<Int> = isDualBatt
        .map { if (it) IS_DUAL_BATTERY else IS_SINGLE_BATTERY }
        .stateIn(viewModelScope, SharingStarted.Eagerly, IS_SINGLE_BATTERY)

    fun setDualBat(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[DUAL_BATTERY_KEY] = enabled }
        }
    }

    val isMultiply: StateFlow<Boolean> = dataStore.data
        .map { it[MULTIPLY_KEY] != false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val selectedMagnitude: StateFlow<Int> = dataStore.data
        .map { it[MULTIPLIER_MAGNITUDE_KEY] ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val calibMultiplier: StateFlow<Double> = combine(isMultiply, selectedMagnitude) { isMult, mag ->
        if (isMult) 10.0.pow(mag.toDouble()) else 1 / 10.0.pow(mag.toDouble())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 1.0)

    fun setMultiplierPrefs(isMultiply: Boolean, magnitude: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[MULTIPLY_KEY] = isMultiply
                it[MULTIPLIER_MAGNITUDE_KEY] = magnitude
            }
        }
    }

    fun refreshBatteryInfo() {
        viewModelScope.launch() {
            val intent =
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
            val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -999) ?: 0
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
            val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
            refreshLock.withLock {
                _batteryInfoList.update {
                    listOf(
                        BatteryInfo(context.getString(R.string.battery_level), "$level%"),
                        BatteryInfo(
                            context.getString(R.string.battery_temperature),
                            "${temperature / 10.0}°C"
                        ),
                        BatteryInfo(
                            context.getString(R.string.battery_status),
                            getStatusString(status, context)
                        ),
                        BatteryInfo(
                            context.getString(R.string.battery_health),
                            getHealthString(health, context)
                        ),
                        BatteryInfo(
                            context.getString(R.string.battery_cycle_count),
                            cycleCount.toString()
                        ),
                    )
                }
            }
        }
    }

    fun refreshBatteryInfoWithRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            var rootModeVoltage0 = 0
            var rootModeVoltage1 = 0
            var rootModeCurrent = 0
            var rootModePower = 0.0
            var rootReadFailed = false
            withContext(Dispatchers.IO) {
                refreshLock.withLock {
                    // fall back to batteryManager if root read fails for rooted vot1,2 and current
                    rootModeVoltage0 =
                        safeRootReadInt(context, "bcc_parms", BCC_VOLTAGE_0_INDEX, {
                            val intent = context.registerReceiver(
                                null,
                                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                            )
                            intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
                        }) { rootReadFailed = true }

                    rootModeVoltage1 =
                        safeRootReadInt(context, "bcc_parms", BCC_VOLTAGE_1_INDEX, {
                            // if root read fails, shows value 0, on the right side of Battery Voltage entry
                            // e.g. 4000 / 0 mV
                            0
                        }) { rootReadFailed = true }

                    rootModeCurrent =
                        safeRootReadInt(context, "bcc_parms", BCC_CURRENT_INDEX, {
                            (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * calibMultiplier.value).toInt()
                        }) { rootReadFailed = true }
                    rootModePower = if (!rootReadFailed) {
                        (rootModeVoltage0 + rootModeVoltage1) * rootModeCurrent * calibMultiplier.value / 1000000.0
                    } else {
                        val intent = context.registerReceiver(
                            null,
                            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                        )

                        (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                                * dualBattMultiplier.value
                                * calibMultiplier.value
                                * (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                            ?: 0) / 1000000.0)
                    }
                    val rm = readBatteryInfo("battery_rm", context)
                        ?: context.getString(R.string.unknown)
                    val fcc = readBatteryInfo("battery_fcc", context)
                        ?: context.getString(R.string.unknown)
                    val soh = readBatteryInfo("battery_soh", context)
                        ?: context.getString(R.string.unknown)
                    val vbatUv = readBatteryInfo("vbat_uv", context)
                        ?: context.getString(R.string.unknown)
                    val sn = readBatteryInfo("battery_sn", context)
                        ?: context.getString(R.string.unknown)
                    val batManDate = readBatteryInfo("battery_manu_date", context)
                        ?: context.getString(R.string.unknown)
                    val rawSoh = calcRawSoh(
                        soh.toIntOrNull() ?: 0,
                        vbatUv.toIntOrNull() ?: 0,
                        readTermCoeff(context)
                    ).let { resultValue ->
                        if (resultValue < 0.0001f) context.getString(R.string.unknown) else resultValue.toString()
                    }
                    val rawFcc = calcRawFcc(
                        fcc.toIntOrNull() ?: 0,
                        rawSoh.toFloatOrNull() ?: 0f,
                        vbatUv.toIntOrNull() ?: 0,
                        readTermCoeff(context)
                    ).let { resultValue ->
                        if (resultValue == 0) context.getString(R.string.unknown) else resultValue.toString()
                    }
                    _batteryInfoList2.update {
                        listOf(
                            BatteryInfo(
                                context.getString(R.string.battery_voltage),
                                "$rootModeVoltage0 / $rootModeVoltage1 mV"
                            ),
                            BatteryInfo(
                                context.getString(R.string.battery_current),
                                "${rootModeCurrent * calibMultiplier.value} mA"
                            ),
                            BatteryInfo(
                                context.getString(R.string.power),
                                String.format("%.2f W", rootModePower)
                            ),
                            BatteryInfo(
                                context.getString(R.string.remaining_charge_counter),
                                "$rm mAh"
                            ),
                            BatteryInfo(
                                context.getString(R.string.full_charge_capacity_battery_fcc),
                                "$fcc mAh"
                            ),
                            BatteryInfo(
                                context.getString(R.string.raw_full_charge_capacity_before_compensation),
                                "$rawFcc mAh"
                            ),
                            BatteryInfo(
                                context.getString(R.string.battery_health_battery_soh),
                                "$soh %"
                            ),
                            BatteryInfo(
                                context.getString(R.string.raw_battery_health_before_compensation),
                                "$rawSoh %"
                            ),
                            BatteryInfo(
                                context.getString(R.string.battery_under_voltage_threshold_vbat_uv),
                                "$vbatUv mV"
                            ),
                            BatteryInfo(
                                context.getString(R.string.battery_serial_number_battery_sn),
                                sn
                            ),
                            BatteryInfo(
                                context.getString(R.string.battery_manufacture_date_battery_manu_date),
                                batManDate
                            )
                            )
                    }
                }
            }
        }
    }

    fun refreshNonRootVoltCurrPwr() {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val current =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * calibMultiplier.value
        val power = current * voltage * dualBattMultiplier.value / 1000000.0
        viewModelScope.launch(Dispatchers.IO) {
            refreshLock.withLock {
                _batteryInfoList3.update {
                    listOf(
                        BatteryInfo(context.getString(R.string.battery_voltage), "$voltage mV"),
                        BatteryInfo(context.getString(R.string.battery_current), "$current mA"),
                        BatteryInfo(
                            context.getString(R.string.power),
                            "${String.format("%.2f W", power)}"
                        )
                    )
                }
            }
        }
    }
}

