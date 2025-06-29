package com.dijia1124.plusplusbattery.data.parser

class XiaomiLogParser : LogParser {
    override val tagFilter = "DeviceStatisticsService"

    private val marker   = Regex("""\bchargerType=""")
    private val kvpRegex = Regex("""(\w+)=([^\s,]+)""")

    override fun matches(line: String): Boolean =
        marker.containsMatchIn(line)

    override fun parse(line: String): Map<String, String> {
        val payload = line.substringAfter(':').trim()
        return kvpRegex.findAll(payload).associate { it.groupValues[1] to it.groupValues[2] }
    }
}
