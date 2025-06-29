package com.dijia1124.plusplusbattery


import com.dijia1124.plusplusbattery.data.parser.FallbackLogParser
import com.dijia1124.plusplusbattery.data.parser.LogParser
import com.dijia1124.plusplusbattery.data.parser.MotoLogParser
import com.dijia1124.plusplusbattery.data.parser.OPlusLogParser
import com.dijia1124.plusplusbattery.data.parser.XiaomiLogParser
import kotlin.test.*                   // assertEquals, assertTrue…
import org.junit.jupiter.params.*
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files

data class Sample(
    val name: String,
    val parser: LogParser,
    val resource: String,
    val keyToAssert: String,
)

class LogParserTest {

    companion object {

        @JvmStatic
        fun samples() = listOf(
            Sample(
                name = "OPlus",
                parser = OPlusLogParser(),
                resource = "logcats/oplus_sample",
                keyToAssert = "fcc"
            ),
            Sample(
                name = "Moto",
                parser = MotoLogParser(),
                resource = "logcats/moto_sample",
                keyToAssert = "POWER_SUPPLY_VOLTAGE_NOW"
            ),
            Sample(
                name = "Xiaomi",
                parser = XiaomiLogParser(),
                resource = "logcats/xiaomi_sample",
                keyToAssert = "batteryLevel"
            ),
//            Sample(
//                name = "Fallback",
//                parser = FallbackLogParser(),
//                resource = "logs/fallback_sample.log",
//                keyToAssert = "voltage_now"
//            )
        )
    }

    @ParameterizedTest(name = "{0} sample should be parsed")
    @MethodSource("samples")
    fun parseWholeFile(sample: Sample) {
        val (name, parser, res, key) = sample

        val url = javaClass.classLoader!!.getResource(res)
            ?: error("Resource $res not found")
        val lines = Files.lines(java.nio.file.Paths.get(url.toURI()))

        val maps = lines.filter(parser::matches)
            .map(parser::parse)
            .toList()

        assertTrue(maps.isNotEmpty(), "$name: no line matched")
        assertTrue(maps.any { it.containsKey(key) },
            "$name: key '$key' not found in parsed maps")
    }
}