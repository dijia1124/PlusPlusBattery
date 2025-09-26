package com.dijia1124.plusplusbattery.data.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.data.model.HistoryInfo
import com.dijia1124.plusplusbattery.data.repository.HistoryInfoRepository
import com.topjohnwu.superuser.Shell
import java.io.File
import com.topjohnwu.superuser.io.SuFileInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

suspend fun readBatteryInfo(field: String, basePath: String = OPLUS_CHG_BATTERY_PATH): String? = withContext(Dispatchers.IO) {
    try {
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
    if (compensatedSoh == 100) return 0f
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
    coeffList: List<Triple<Int, Int, Int>>,
    designCapacity: Int
): Int {
    if (compensatedFcc == designCapacity) return 0
    if (rawSoh == 0f) return 0
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
    val headLine  = readBatteryInfo("battery_log_head") ?: return@withContext emptyMap<String, String>()
    val valueLine = readBatteryInfo("battery_log_content") ?: return@withContext emptyMap<String, String>()

    val heads  = headLine.split(',')
    val values = valueLine.split(',')
    if (heads.size != values.size) return@withContext emptyMap<String, String>()

    heads.indices
        .filter { it > 0 && (fields == null || heads[it] in fields) }
        .associate { idx -> heads[idx] to values[idx] }
}


suspend fun safeRootReadInt(
    path: String,
    index: Int,
    fallback: () -> Int,
    onFallback: () -> Unit
): Int = withContext(Dispatchers.IO) {
    try {
        val valueStr = readBatteryInfo(path, index)
        val parsed = valueStr?.toIntOrNull()
        if (parsed != null) {
            parsed
        } else {
            onFallback()
            fallback()
        }
    } catch (e: Exception) {
        onFallback()
        fallback()
    }
}

suspend fun isDualBattery(): Boolean = withContext(Dispatchers.IO) {
    // detect battery cells with root access
    val line = readBatteryInfo("aging_ffc_data") ?: return@withContext false

    // expected format: 0,2,0,0,186,4550,4490,4520,4475
    val parts = line.split(',').map { it.trim() }
    if (parts.size > 1) {
        when (parts[1]) {
            "2" -> true
            "1" -> false
            else -> false
        }
    } else false
}

fun normalizeQmax(rawQ: Int, fcc: Int?): Int {
    // some models report qmax multiplied by 10
    var q = rawQ
    val ref = fcc ?: 20000
    while (q >= ref * 2) {
        q /= 10
    }
    return q
}

suspend fun saveCycleCountToHistory(context: Context, historyInfoRepository: HistoryInfoRepository) {
    withContext(Dispatchers.IO) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1
        val date = System.currentTimeMillis()
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(date))
        val newInfo = HistoryInfo(
            date = date,
            dateString = dateString,
            cycleCount = cycleCount.toString()
        )
        historyInfoRepository.insertOrUpdate(newInfo)
    }
}