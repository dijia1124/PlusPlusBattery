package com.example.plusplusbattery

import android.content.Context
import android.os.BatteryManager
import android.util.Base64
import com.topjohnwu.superuser.Shell
import java.nio.ByteBuffer

fun getStatusString(status: Int, context: Context): String = when (status) {
    BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.charging)
    BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.discharging)
    BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.full)
    else -> context.getString(R.string.not_charging)
}

fun getHealthString(health: Int, context: Context): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.good)
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.overheat)
    BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.dead)
    else -> context.getString(R.string.unknown)
}

fun getBoolString(boolVal: Boolean, context: Context): String = when(boolVal) {
    true -> context.getString(R.string.yes)
    false -> context.getString(R.string.no)
}

fun readBatteryInfo(field: String, context: Context): String {
    val basePath = "/sys/class/oplus_chg/battery/"
    val fullPath = basePath + field

    return try {
        val result = Shell.cmd("cat $fullPath").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            result.out.joinToString()
        } else {
            context.getString(R.string.unknown)
        }
    } catch (e: Exception) {
        context.getString(R.string.unknown)
    }
}


fun readTermCoeff(context: Context): List<Triple<Int, Int, Int>> {
    val battType = readBatteryInfo("battery_type", context)
    val path = "/proc/device-tree/soc/oplus,mms_gauge/$battType/deep_spec,term_coeff"
    val altPath = "/proc/device-tree/soc/oplus,mms_gauge/deep_spec,term_coeff"

    val selectedPath = when {
        Shell.cmd("[ -f $path ] && echo exists").exec().out.joinToString("").trim() == "exists" -> path
        Shell.cmd("[ -f $altPath ] && echo exists").exec().out.joinToString("").trim() == "exists" -> altPath
        else -> null
    }

    return try {
        val result = Shell.cmd("base64 $selectedPath").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            val base64Str = result.out.joinToString("")
            val bytes = Base64.decode(base64Str, Base64.DEFAULT)

            val buffer = ByteBuffer.wrap(bytes)
            val list = mutableListOf<Triple<Int, Int, Int>>()
            while (buffer.remaining() >= 12) {
                val vbatUv = buffer.int
                val fccOffset = buffer.int
                val sohOffset = buffer.int
                list.add(Triple(vbatUv, fccOffset, sohOffset))
            }
            list
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun calcRawSoh(
    compensatedSoh: Int,
    vbatUv: Int,
    coeffList: List<Triple<Int, Int, Int>>
): Float {
    val match = coeffList.find { it.first == vbatUv }

    return if (match != null) {
        val sohOffset = match.third
        val factor = 1 + sohOffset.toFloat() / 100f
        compensatedSoh.toFloat() / factor
    } else {
        compensatedSoh.toFloat()
    }
}

fun calcRawFcc(
    compensatedFcc: Int,
    rawSoh: Float,
    vbatUv: Int,
    coeffList: List<Triple<Int, Int, Int>>
): Int {
    val match = coeffList.find { it.first == vbatUv }

    return if (match != null) {
        val fccOffset = match.second
        compensatedFcc - (fccOffset * rawSoh.toInt() / 100)
    } else {
        compensatedFcc
    }
}
