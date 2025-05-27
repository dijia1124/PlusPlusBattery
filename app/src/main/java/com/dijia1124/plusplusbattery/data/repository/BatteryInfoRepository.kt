package com.dijia1124.plusplusbattery.data.repository

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.datastore.preferences.core.edit
import com.dijia1124.plusplusbattery.data.util.DUAL_BATTERY_KEY
import com.dijia1124.plusplusbattery.data.util.ESTIMATED_FCC_KEY
import com.dijia1124.plusplusbattery.data.util.MULTIPLIER_MAGNITUDE_KEY
import com.dijia1124.plusplusbattery.data.util.MULTIPLY_KEY
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.data.util.calcRawFcc
import com.dijia1124.plusplusbattery.data.util.calcRawSoh
import com.dijia1124.plusplusbattery.data.model.BatteryInfo
import com.dijia1124.plusplusbattery.data.util.dataStore
import com.dijia1124.plusplusbattery.data.util.formatWithUnit
import com.dijia1124.plusplusbattery.data.util.getHealthString
import com.dijia1124.plusplusbattery.data.util.getStatusString
import com.dijia1124.plusplusbattery.data.util.readBatteryInfo
import com.dijia1124.plusplusbattery.data.util.readBatteryLogMap
import com.dijia1124.plusplusbattery.data.util.readTermCoeff
import com.dijia1124.plusplusbattery.data.util.safeRootReadInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.pow

private const val BCC_VOLTAGE_0_INDEX = 6
private const val BCC_VOLTAGE_1_INDEX = 11
private const val BCC_CURRENT_INDEX = 8

class BatteryInfoRepository(private val context: Context) {
    private val batteryManager get() =
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    private val settings by lazy { context.dataStore }

    private val calibFlow = settings.data
        .map { prefs ->
            val isMult = prefs[MULTIPLY_KEY] != false
            val mag    = prefs[MULTIPLIER_MAGNITUDE_KEY] ?: 0
            if (isMult) 10.0.pow(mag.toDouble()) else 1/10.0.pow(mag.toDouble())
        }

    private val dualBattFlow: Flow<Int> = settings.data
        .map { prefs -> if (prefs[DUAL_BATTERY_KEY] == true) 2 else 1 }

    val isDualBattFlow: Flow<Boolean> = dualBattFlow
        .map { it == 2 }

    suspend fun setDualBatt(enabled: Boolean) {
        settings.edit { it[DUAL_BATTERY_KEY] = enabled }
    }

    suspend fun setMultiplierPrefs(isMult: Boolean, mag: Int) {
        settings.edit { prefs ->
            prefs[MULTIPLY_KEY] = isMult
            prefs[MULTIPLIER_MAGNITUDE_KEY] = mag
        }
    }

    val isMultiplyFlow: Flow<Boolean> = settings.data
        .map { prefs -> prefs[MULTIPLY_KEY] != false }

    val selectedMagnitudeFlow: Flow<Int> = settings.data
        .map { prefs -> prefs[MULTIPLIER_MAGNITUDE_KEY] ?: 0 }

    val estimatedFccFlow: Flow<String> = settings.data
        .map { prefs ->
            prefs[ESTIMATED_FCC_KEY]?.toString()
                ?: context.getString(R.string.estimating_full_charge_capacity)
        }

    suspend fun getBasicBatteryInfo(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val temperature = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -999) ?: 0
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
        val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        listOf(
            BatteryInfo(context.getString(R.string.battery_level), "$level %"),
            BatteryInfo(
                context.getString(R.string.battery_temperature),
                "${temperature / 10.0} °C"
            ),
            BatteryInfo(
                context.getString(R.string.battery_status),
                getStatusString(status, context)
            ),
            BatteryInfo(
                context.getString(R.string.battery_health),
                getHealthString(health, context)
            ),
            BatteryInfo(context.getString(R.string.battery_cycle_count), cycleCount.toString()),
        )
    }

    suspend fun getRootBatteryInfo(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val calibMultiplier = calibFlow.first()
        val dualBattMultiplier = dualBattFlow.first()
        var rootModeVoltage0 = 0
        var rootModeVoltage1 = 0
        var rootModeCurrent = 0
        var rootModePower = 0.0
        var rootReadFailed = false
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
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW).toInt()
            }) { rootReadFailed = true }
        rootModePower = if (!rootReadFailed) {
            (rootModeVoltage0 + rootModeVoltage1) * rootModeCurrent * calibMultiplier / 1000000.0
        } else {
            val intent = context.registerReceiver(
                null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            )

            (batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    * dualBattMultiplier
                    * calibMultiplier
                    * (intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                ?: 0) / 1000000.0)
        }
        val rm = readBatteryInfo("battery_rm")
            ?: context.getString(R.string.unknown)
        val fcc = readBatteryInfo("battery_fcc")
            ?: context.getString(R.string.unknown)
        val soh = readBatteryInfo("battery_soh")
            ?: context.getString(R.string.unknown)
        val vbatUv = readBatteryInfo("vbat_uv")
            ?: context.getString(R.string.unknown)
        val sn = readBatteryInfo("battery_sn")
            ?: context.getString(R.string.unknown)
        val batManDate = readBatteryInfo("battery_manu_date")
            ?: context.getString(R.string.unknown)
        val battType = readBatteryInfo("battery_type")
            ?: context.getString(R.string.unknown)
        val designCapacity = readBatteryInfo("design_capacity")
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
        val logMap = readBatteryLogMap()
        val qMax = logMap["batt_qmax"]?.let { "$it mAh" } ?: context.getString(R.string.unknown)
        listOf(
            BatteryInfo(
                context.getString(R.string.battery_voltage),
                "$rootModeVoltage0 / $rootModeVoltage1 mV"
            ),
            BatteryInfo(
                context.getString(R.string.battery_current),
                (rootModeCurrent * calibMultiplier).formatWithUnit("mA")
            ),
            BatteryInfo(
                context.getString(R.string.power),
                rootModePower.formatWithUnit("W")
            ),
            BatteryInfo(
                context.getString(R.string.remaining_charge_counter),
                "$rm mAh",
                "battery_rm"
            ),
            BatteryInfo(
                context.getString(R.string.full_charge_capacity_battery_fcc),
                "$fcc mAh",
                "battery_fcc"
            ),
            BatteryInfo(
                context.getString(R.string.raw_full_charge_capacity_before_compensation),
                "$rawFcc mAh",
                "battery_fcc (raw)"
            ),
            BatteryInfo(
                context.getString(R.string.battery_health_battery_soh),
                "$soh %",
                "battery_soh"
            ),
            BatteryInfo(
                context.getString(R.string.raw_battery_health_before_compensation),
                rawSoh
                    .toDoubleOrNull()
                    ?.formatWithUnit("%")
                    ?: rawSoh,
                "battery_soh (raw)"
            ),
            BatteryInfo(
                context.getString(R.string.battery_qmax),
                qMax,
                "batt_qmax"
            ),
            BatteryInfo(
                context.getString(R.string.battery_under_voltage_threshold_vbat_uv),
                "$vbatUv mV",
                "vbat_uv"
            ),
            BatteryInfo(
                context.getString(R.string.battery_serial_number_battery_sn),
                sn,
                "battery_sn"
            ),
            BatteryInfo(
                context.getString(R.string.battery_manufacture_date_battery_manu_date),
                batManDate,
                "battery_manu_date"
            ),
            BatteryInfo(
                context.getString(R.string.battery_type_battery_type),
                battType,
                "battery_type"
            ),
            BatteryInfo(
                context.getString(R.string.design_capacity_design_capacity),
                "$designCapacity mAh",
                "design_capacity"
            ),
        )
    }

    suspend fun getNonRootVoltCurrPwr(): List<BatteryInfo> = withContext(Dispatchers.IO) {
        val calibMultiplier = calibFlow.first()
        val dualBattMultiplier = dualBattFlow.first()
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val current =
            batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * calibMultiplier
        val power = current * voltage * dualBattMultiplier / 1_000_000.0

        listOf(
            BatteryInfo(context.getString(R.string.battery_voltage), "$voltage mV"),
            BatteryInfo(context.getString(R.string.battery_current), current.formatWithUnit("mA")),
            BatteryInfo(context.getString(R.string.power), power.formatWithUnit("W"))
        )
    }

    suspend fun getEstimatedFcc(savedEstimatedFcc: String): BatteryInfo =
        withContext(Dispatchers.IO) {
            val currentNow =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val batteryLevel =
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            if (currentNow == 0 && batteryLevel == 100) {
                val chargeCounter =
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                val fullChargeCapacity = (chargeCounter / (batteryLevel / 100.0)).toInt() / 1000
                if (fullChargeCapacity > 0) {
                    context.dataStore.edit { prefs ->
                        prefs[ESTIMATED_FCC_KEY] = fullChargeCapacity
                    }
                }
            }
            var estimatedFcc = context.getString(R.string.estimating_full_charge_capacity)

            if (savedEstimatedFcc != context.getString(R.string.estimating_full_charge_capacity)) {
                estimatedFcc = savedEstimatedFcc
                BatteryInfo(
                    context.getString(R.string.full_charge_capacity),
                    "$estimatedFcc mAh"
                )
            } else {
                BatteryInfo(
                    context.getString(R.string.full_charge_capacity),
                    estimatedFcc
                )
            }
        }
}