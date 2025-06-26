package com.dijia1124.plusplusbattery.data.parser

/**
 * Defines a parser that can match specific logcat lines and extract key=value pairs.
 */
interface LogParser {
    /** e.g. "BatteryService", "charge_time" */
    val tagFilter: String

    /** cheap check to see if this parser wants to look at the line */
    fun matches(line: String): Boolean

    /** extract key→value map */
    fun parse(line: String): Map<String,String>
}

