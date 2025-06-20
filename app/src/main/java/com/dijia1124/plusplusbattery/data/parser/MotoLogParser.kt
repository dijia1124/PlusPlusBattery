package com.dijia1124.plusplusbattery.data.parser

import android.util.Log

/**
 * Parses Motorola BatteryService uevent blocks:
 * D BatteryService: uevent={KEY=val,KEY=val,...}
 */
class MotoLogParser : LogParser {
    override val tagFilter = "BatteryService"

    // grab exactly what's inside the braces { ... }
    private val payloadRe = Regex(
        """uevent\s*=\s*\{([^}]*)\}""",
        RegexOption.IGNORE_CASE
    )

    override fun matches(line: String): Boolean =
        payloadRe.containsMatchIn(line)

    override fun parse(line: String): Map<String, String> {
        // pull out the comma-separated payload
        val payload = payloadRe.find(line)
            ?.groupValues
            ?.get(1)
            ?: return emptyMap()
        return payload
            .split(',')
            .mapNotNull { segment ->
                val parts = segment.split('=', limit = 2).map(String::trim)
                val k = parts.getOrNull(0)?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                k to parts.getOrNull(1).orEmpty()
            }
            .toMap()
    }
}
