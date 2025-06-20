package com.dijia1124.plusplusbattery.data.parser

class OPlusLogParser : LogParser {
    // 1️⃣ Used only by logcat for pre-filtering
    override val tagFilter = "charge_time"

    // 2️⃣ We now look for “type=” to ensure it’s the payload line
    private val payloadMarker = Regex("""\btype\s*=""", RegexOption.IGNORE_CASE)
    private val kvRe = Regex("""\b(\w+)=(.*?)(?=,|$)""")

    override fun matches(line: String) = payloadMarker.containsMatchIn(line)

    override fun parse(line: String): Map<String, String> =
        kvRe.findAll(line).associate { it.groupValues[1] to it.groupValues[2].trim() }
}
