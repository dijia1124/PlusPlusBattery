package com.dijia1124.plusplusbattery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dijia1124.plusplusbattery.data.model.BatteryInfo
import com.dijia1124.plusplusbattery.ui.screen.NormalBatteryCard

data class PowerDataPoint(
    val timestamp: Long,
    val power: Float,
    val temperature: Float = 0f
)

@Composable
fun CardWithPowerChart(
    info: BatteryInfo,
    powerData: List<PowerDataPoint>
) {
    Column() {
        NormalBatteryCard(info)
        if (powerData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            PowerChart(
                data = powerData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun PowerChart(
    data: List<PowerDataPoint>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val powerLineColor = MaterialTheme.colorScheme.primary
    val temperatureLineColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val topPadding = with(density) { 12.dp.toPx() }
        val bottomPadding = with(density) { 32.dp.toPx() }

        if (data.isEmpty()) return@Canvas

        // Calculate data ranges
        val minTime = data.first().timestamp
        val maxTime = data.last().timestamp
        val rawMaxPower = data.maxOfOrNull { it.power } ?: 1f
        val rawMinTemp = data.minOfOrNull { it.temperature } ?: 0f
        val rawMaxTemp = data.maxOfOrNull { it.temperature } ?: 1f

        // Handle power range
        val minPower = 0f
        val maxPower = kotlin.math.ceil(kotlin.math.max(rawMaxPower * 1.1f, 5f)).toFloat()
        val adjustedPowerRange = maxPower - minPower

        // Handle temperature range
        val tempMargin = kotlin.math.max((rawMaxTemp - rawMinTemp) * 0.1f, 5f)
        val minTemp = kotlin.math.floor(rawMinTemp - tempMargin).toFloat()
        val maxTemp = kotlin.math.ceil(rawMaxTemp + tempMargin).toFloat()
        val adjustedTempRange = maxTemp - minTemp

        // Handle time range
        val rawTimeRange = maxTime - minTime
        val displayTimeRange = if (rawTimeRange < 30000) {
            kotlin.math.max(rawTimeRange, 30000)
        } else {
            rawTimeRange
        }

        // Text paint for Y-axis labels
        val textPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        // Calculate maximum width needed for left Y-axis labels
        var maxLeftLabelWidth = 0f
        for (i in 0..4) {
            val powerValue = maxPower - (adjustedPowerRange * i / 4)
            val text = powerValue.toInt().toString()
            val textWidth = textPaint.measureText(text)
            maxLeftLabelWidth = kotlin.math.max(maxLeftLabelWidth, textWidth)
        }

        // Text paint for right Y-axis labels
        val rightTextPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.LEFT
        }

        // Calculate maximum width needed for right Y-axis labels
        var maxRightLabelWidth = 0f
        for (i in 0..4) {
            val tempValue = maxTemp - (adjustedTempRange * i / 4)
            val text = tempValue.toInt().toString()
            val textWidth = rightTextPaint.measureText(text)
            maxRightLabelWidth = kotlin.math.max(maxRightLabelWidth, textWidth)
        }

        // Dynamic padding based on label widths
        val leftPadding = maxLeftLabelWidth + with(density) { 25.dp.toPx() }
        val rightPadding = maxRightLabelWidth + with(density) { 25.dp.toPx() }

        // Drawing area
        val chartLeft = leftPadding
        val chartRight = canvasWidth - rightPadding
        val chartTop = topPadding
        val chartBottom = canvasHeight - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Draw grid lines and Y-axis labels
        for (i in 0..4) {
            val y = chartTop + (chartHeight * i / 4)
            val powerValue = maxPower - (adjustedPowerRange * i / 4)
            val tempValue = maxTemp - (adjustedTempRange * i / 4)

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 0.5.dp.toPx()
            )

            // Left Y-axis label (Power)
            drawIntoCanvas { canvas ->
                val text = "${powerValue.toInt()} W"
                canvas.nativeCanvas.drawText(
                    text,
                    chartLeft - with(density) { 8.dp.toPx() },
                    y + with(density) { 3.dp.toPx() },
                    textPaint
                )
            }

            // Right Y-axis label (Temp)
            drawIntoCanvas { canvas ->
                val text = "${tempValue.toInt()}°C"
                canvas.nativeCanvas.drawText(
                    text,
                    chartRight + with(density) { 8.dp.toPx() },
                    y + with(density) { 3.dp.toPx() },
                    rightTextPaint
                )
            }
        }

        // Text paint for X-axis labels
        val xAxisTextPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw X-axis labels
        val timeSteps = 4
        for (i in 0..timeSteps) {
            val x = chartLeft + (chartWidth * i / timeSteps)
            val timeValue = (displayTimeRange * i / timeSteps) / 1000

            drawIntoCanvas { canvas ->
                val text = if (timeValue >= 60) {
                    val minutes = (timeValue / 60).toInt()
                    val seconds = (timeValue % 60).toInt()
                    "${minutes}m${seconds}s"
                } else {
                    "${timeValue.toInt()}s"
                }
                canvas.nativeCanvas.drawText(
                    text,
                    x,
                    chartBottom + with(density) { 20.dp.toPx() },
                    xAxisTextPaint
                )
            }
        }

        // Draw power line
        if (data.size >= 2) {
            val powerPath = Path()
            data.forEachIndexed { index, point ->
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val y = chartBottom - (chartHeight * (point.power - minPower) / adjustedPowerRange.coerceAtLeast(0.01f))

                if (index == 0) {
                    powerPath.moveTo(x, y)
                } else {
                    powerPath.lineTo(x, y)
                }
            }

            drawPath(
                path = powerPath,
                color = powerLineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw temperature line
        if (data.size >= 2) {
            val tempPath = Path()
            data.forEachIndexed { index, point ->
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val y = chartBottom - (chartHeight * (point.temperature - minTemp) / adjustedTempRange.coerceAtLeast(0.01f))

                if (index == 0) {
                    tempPath.moveTo(x, y)
                } else {
                    tempPath.lineTo(x, y)
                }
            }

            drawPath(
                path = tempPath,
                color = temperatureLineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw power data points
        data.forEach { point ->
            val relativeTimestamp = point.timestamp - minTime
            val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
            val y = chartBottom - (chartHeight * (point.power - minPower) / adjustedPowerRange.coerceAtLeast(0.01f))

            val pointRadius = when {
                data.size > 30 -> 1.dp.toPx()
                else -> 2.dp.toPx()
            }

            drawCircle(
                color = powerLineColor,
                radius = pointRadius,
                center = Offset(x, y)
            )
        }

        // Draw temperature data points
        data.forEach { point ->
            val relativeTimestamp = point.timestamp - minTime
            val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
            val y = chartBottom - (chartHeight * (point.temperature - minTemp) / adjustedTempRange.coerceAtLeast(0.01f))

            val pointRadius = when {
                data.size > 30 -> 1.dp.toPx()
                else -> 2.dp.toPx()
            }

            drawCircle(
                color = temperatureLineColor,
                radius = pointRadius,
                center = Offset(x, y)
            )
        }
    }
}
