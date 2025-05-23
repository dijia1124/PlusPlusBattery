package com.example.plusplusbattery

import android.content.Context
import android.os.BatteryManager
import com.topjohnwu.superuser.Shell
import java.nio.ByteBuffer
import java.io.File
import com.topjohnwu.superuser.io.SuFileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

private const val BCC_CURRENT_INDICES_LAST = 18
private const val OPLUS_CHG_BATTERY_PATH = "/sys/class/oplus_chg/battery/"

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

fun Double.formatClean(fractionDigits: Int = 2): String {
    return if (this % 1.0 == 0.0) {
        // hide decimal point for integer
        "%d".format(this.toLong())
    } else {
        // show decimal point for double
        "%.${fractionDigits}f".format(this)
    }
}

fun Double.formatWithUnit(unit: String, fractionDigits: Int = 2): String =
    "${this.formatClean(fractionDigits)} $unit"

suspend fun readBatteryInfo(field: String): String? = withContext(Dispatchers.IO) {
    try {
        val basePath = OPLUS_CHG_BATTERY_PATH
        SuFileInputStream.open(basePath + field).bufferedReader().use { it.readText().trim() }
    } catch (e: IOException) {
        null
    }
}

suspend fun readBatteryInfo(field: String, index: Int): String? = withContext(Dispatchers.IO) {
    try {
        val raw = SuFileInputStream
            .open("$OPLUS_CHG_BATTERY_PATH$field")
            .bufferedReader()
            .use { it.readText().trim() }

        val parts = raw.split(",")
        if (field == "bcc_parms" && parts.size - 1 != BCC_CURRENT_INDICES_LAST) {
            null
        } else {
            parts.getOrNull(index)?.trim()
        }
    } catch (e: IOException) {
        null
    }
}

suspend fun readTermCoeff(context: Context): List<Triple<Int, Int, Int>> = withContext(Dispatchers.IO) {
    // Make a temporary directory in the app's private storage to avoid extra permission
    val tmpDir = File(context.getExternalFilesDir(null), "read_term_coeff")
    if (!tmpDir.exists()) tmpDir.mkdirs()
    val targetFile = File(tmpDir, "term_coeff")
    val targetPath = targetFile.absolutePath

    val battType = readBatteryInfo("battery_type")
    val primaryPath = "/proc/device-tree/soc/oplus,mms_gauge/$battType/deep_spec,term_coeff"
    val fallbackPath = "/proc/device-tree/soc/oplus,mms_gauge/deep_spec,term_coeff"
    var sourcePath = primaryPath

    val checkPrimary = Shell.cmd("[ -f \"$primaryPath\" ] && echo exists").exec().out.joinToString("").trim()
    if (checkPrimary != "exists") {
        val checkFallback = Shell.cmd("[ -f \"$fallbackPath\" ] && echo exists").exec().out.joinToString("").trim()
        if (checkFallback == "exists") {
            sourcePath = fallbackPath
        } else {
            return@withContext emptyList<Triple<Int, Int, Int>>()
        }
    }

    val ddResult = Shell.cmd("su -c 'dd if=$sourcePath of=$targetPath'").exec()
    if (!ddResult.isSuccess) {
        return@withContext emptyList<Triple<Int, Int, Int>>()
    }

    try {
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

// usage: val logMap = readBatteryLogMap(context)
// val qMax = logMap["batt_qmax"]
suspend fun readBatteryLogMap(
    fields: Set<String>? = null
): Map<String, String> = withContext(Dispatchers.IO) {
    val base = OPLUS_CHG_BATTERY_PATH

    val headLine  = readBatteryInfo("battery_log_head") ?: return@withContext emptyMap<String, String>()
    val valueLine = readBatteryInfo("battery_log_content") ?: return@withContext emptyMap<String, String>()

    val heads  = headLine.split(',')
    val values = valueLine.split(',')
    if (heads.size != values.size) return@withContext emptyMap<String, String>()

    heads.indices
        .filter { it > 0 && (fields == null || heads[it] in fields) }
        .associate { idx -> heads[idx] to values[idx] }
}