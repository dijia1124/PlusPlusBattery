package com.dijia1124.plusplusbattery.data.model

import androidx.annotation.StringRes
import com.dijia1124.plusplusbattery.R

enum class BatteryInfoType(
    val key: String,
    @StringRes val titleRes: Int
) {
    LEVEL              ("Level",                     R.string.battery_level),
    TEMP               ("Temperature",               R.string.battery_temperature),
    STATUS             ("Status",                    R.string.battery_status),
    HEALTH             ("Health",                    R.string.battery_health),
    CYCLE_COUNT        ("Cycle Count",               R.string.battery_cycle_count),
    VOLTAGE            ("Voltage",                   R.string.battery_voltage),
    CURRENT            ("Current",                   R.string.battery_current),
    POWER              ("Power",                     R.string.battery_power),
    EST_FCC            ("Estimated fcc",             R.string.full_charge_capacity),
    RM                 ("battery_rm",                R.string.remaining_charge_counter),
    FCC                ("battery_fcc",               R.string.full_charge_capacity_battery_fcc),
    RAW_FCC            ("battery_fcc (raw)",         R.string.raw_full_charge_capacity_before_compensation),
    SOH                ("battery_soh",               R.string.battery_health_battery_soh),
    RAW_SOH            ("battery_soh (raw)",         R.string.raw_battery_health_before_compensation),
    QMAX               ("batt_qmax",                 R.string.battery_qmax),
    VBAT_UV            ("vbat_uv",                   R.string.battery_under_voltage_threshold_vbat_uv),
    SN                 ("battery_sn",                R.string.battery_serial_number_battery_sn),
    MANU_DATE          ("battery_manu_date",         R.string.battery_manufacture_date_battery_manu_date),
    BATTERY_TYPE       ("battery_type",              R.string.battery_type_battery_type),
    DESIGN_CAPACITY    ("design_capacity",           R.string.design_capacity_design_capacity)
}
