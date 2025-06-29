package com.dijia1124.plusplusbattery.data.repository

import android.os.Build
import android.util.Log
import com.dijia1124.plusplusbattery.data.parser.FallbackLogParser
import com.dijia1124.plusplusbattery.data.parser.LogParser
import com.dijia1124.plusplusbattery.data.parser.MotoLogParser
import com.dijia1124.plusplusbattery.data.parser.OPlusLogParser
import com.dijia1124.plusplusbattery.data.parser.XiaomiLogParser
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BatteryLogRepository {
    private val oplusParsers = listOf(OPlusLogParser(), FallbackLogParser())

    private val allParsers: Map<String, List<LogParser>> = mapOf(
        "motorola" to listOf(MotoLogParser(), FallbackLogParser()),
        "oneplus" to oplusParsers,
        "oppo" to oplusParsers,
        "realme" to oplusParsers,
        "xiaomi" to listOf(XiaomiLogParser(), FallbackLogParser()),
        // add more OEMs here as needed
    )

    private val defaultParsers = listOf(FallbackLogParser())


    suspend fun getParsedLogcatData(): Map<String, String>? =
        getParsedDataFromLines(fetchLiveLogcatLines())

    private suspend fun fetchLiveLogcatLines(): List<String> = withContext(Dispatchers.IO) {
        val parsers = allParsers[Build.MANUFACTURER.lowercase()] ?: defaultParsers
        val filters = parsers.map { "${it.tagFilter}:D" } + "*:S"
        Log.d("DBG", "MANUFACTURER = ${Build.MANUFACTURER}")       // e.g. OnePlus
        Log.d("DBG", "Parsers      = ${parsers.map { it::class.simpleName }}")

        val cmd = "logcat -d -v raw " +
                parsers.joinToString(" ") { "${it.tagFilter}:D" } + " *:S"
        Log.d("DBG", "CMD = $cmd")

        Shell.cmd("logcat -d -v raw ${filters.joinToString(" ")}").exec().out
    }

    suspend fun getParsedDataFromLines(lines: List<String>): Map<String, String>? =
        withContext(Dispatchers.Default) {
            Log.d("DBG", "getParsedDataFromLines: ${lines.size} lines")
            val parsers = allParsers[Build.MANUFACTURER.lowercase()] ?: defaultParsers
            lines.asReversed().firstNotNullOfOrNull { line ->
                parsers.firstNotNullOfOrNull { p ->
                    p.takeIf { it.matches(line) }?.parse(line)?.takeIf { it.isNotEmpty() }
                }
            }
        }
}
