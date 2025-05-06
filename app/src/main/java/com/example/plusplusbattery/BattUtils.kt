package com.example.plusplusbattery

import android.content.Context
import android.os.BatteryManager
import com.topjohnwu.superuser.Shell
import java.nio.ByteBuffer
import java.io.File

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

fun readBatteryInfo(field: String, context: Context): String? {
    val basePath = "/sys/class/oplus_chg/battery/"
    val fullPath = basePath + field

    return try {
        val result = Shell.cmd("cat $fullPath").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            result.out.joinToString()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

fun readBatteryInfo(field: String, context: Context, index: Int): String? {
    val basePath = "/sys/class/oplus_chg/battery/"
    val fullPath = basePath + field
    return try {
        val result = Shell.cmd("su -c cat $fullPath").exec()
        if (result.isSuccess && result.out.isNotEmpty()) {
            val line = result.out.joinToString("").trim()
            val values = line.split(",")
            if (index in values.indices) {
                values[index].trim()
            } else {
                null
            }
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}


fun readTermCoeff(context: Context): List<Triple<Int, Int, Int>> {
    // Make a temporary directory in the app's private storage to avoid extra permission
    val tmpDir = File(context.getExternalFilesDir(null), "read_term_coeff")
    if (!tmpDir.exists()) tmpDir.mkdirs()
    val targetFile = File(tmpDir, "term_coeff")
    val targetPath = targetFile.absolutePath

    val battType = readBatteryInfo("battery_type", context)
    val primaryPath = "/proc/device-tree/soc/oplus,mms_gauge/$battType/deep_spec,term_coeff"
    val fallbackPath = "/proc/device-tree/soc/oplus,mms_gauge/deep_spec,term_coeff"
    var sourcePath = primaryPath

    val checkPrimary = Shell.cmd("[ -f \"$primaryPath\" ] && echo exists").exec().out.joinToString("").trim()
    if (checkPrimary != "exists") {
        val checkFallback = Shell.cmd("[ -f \"$fallbackPath\" ] && echo exists").exec().out.joinToString("").trim()
        if (checkFallback == "exists") {
            sourcePath = fallbackPath
        } else {
            return emptyList()
        }
    }

    val ddResult = Shell.cmd("su -c dd if=$sourcePath of=$targetPath").exec()
    if (!ddResult.isSuccess) {
        return emptyList()
    }

    return try {
        val bytes = targetFile.readBytes()

        val buffer = ByteBuffer.wrap(bytes)
        val list = mutableListOf<Triple<Int, Int, Int>>()
        while (buffer.remaining() >= 12) {
            val vbatUv = buffer.int
            val fccOffset = buffer.int
            val sohOffset = buffer.int
            list.add(Triple(vbatUv, fccOffset, sohOffset))
        }
        list
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