package com.dijia1124.plusplusbattery.data.parser

import android.util.Log

/**
 * Fallback parser for any line with key=value segments (after a colon).
 */
class FallbackLogParser : LogParser {
    override val tagFilter = "*"
    override fun matches(line: String): Boolean {
        // Ensure it has some key=value and is not caught by other parsers
        return line.contains('=') && !line.contains("BatteryService") && !line.contains("charge_time:")
    }

    override fun parse(line: String): Map<String, String> {
         Log.d("FallbackLogParser", "Parsing line")
        // get text after last colon
        val payload = line.substringAfterLast(':').trim()
        return payload
            .split(',', ' ')
            .mapNotNull { segment ->
                val parts = segment.split('=', limit = 2).map(String::trim)
                val key = parts.getOrNull(0)?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                key to parts.getOrNull(1).orEmpty()
            }
            .toMap()
    }
}
