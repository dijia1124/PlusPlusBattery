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
    val power: Float
)

@Composable
fun CardWithPowerChart(
    info: BatteryInfo,
    powerData: List<PowerDataPoint>
) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        NormalBatteryCard(info)
        if (powerData.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            PowerChart(
                data = powerData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
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
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = with(density) { 24.dp.toPx() }
        val bottomPadding = with(density) { 32.dp.toPx() }

        if (data.isEmpty()) return@Canvas

        // Calculate data ranges
        val minTime = data.first().timestamp
        val maxTime = data.last().timestamp
        val rawMinPower = data.minOfOrNull { it.power } ?: 0f
        val rawMaxPower = data.maxOfOrNull { it.power } ?: 1f

        // Handle power range - always start from 0 and use integer scales
        val minPower = 0f // Always start from 0
        val maxPower = kotlin.math.ceil(kotlin.math.max(rawMaxPower * 1.1f, 5f)).toFloat() // Add 10% padding, minimum 5

        val adjustedPowerRange = maxPower - minPower

        // Handle time range - ensure minimum display range
        val rawTimeRange = maxTime - minTime
        val displayTimeRange = if (rawTimeRange < 30000) { // Less than 30 seconds
            kotlin.math.max(rawTimeRange, 30000) // At least 30 seconds
        } else {
            rawTimeRange
        }

        // Text paint for measuring and drawing Y-axis labels
        val textPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        // Calculate maximum width needed for Y-axis labels (using integer format)
        var maxLabelWidth = 0f
        for (i in 0..4) {
            val powerValue = maxPower - (adjustedPowerRange * i / 4)
            val text = powerValue.toInt().toString() // Always show as integer
            val textWidth = textPaint.measureText(text)
            maxLabelWidth = kotlin.math.max(maxLabelWidth, textWidth)
        }

        // Dynamic left padding based on label width
        val leftPadding = maxLabelWidth + with(density) { 12.dp.toPx() }

        // Drawing area
        val chartLeft = leftPadding
        val chartRight = canvasWidth - padding
        val chartTop = padding
        val chartBottom = canvasHeight - bottomPadding
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Draw grid lines and Y-axis labels
        for (i in 0..4) {
            val y = chartTop + (chartHeight * i / 4)
            val powerValue = maxPower - (adjustedPowerRange * i / 4)

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 0.5.dp.toPx()
            )

            // Y-axis label (always integer)
            drawIntoCanvas { canvas ->
                val text = powerValue.toInt().toString()
                canvas.nativeCanvas.drawText(
                    text,
                    chartLeft - with(density) { 8.dp.toPx() },
                    y + with(density) { 3.dp.toPx() },
                    textPaint
                )
            }
        }

        // Text paint for X-axis labels (center aligned)
        val xAxisTextPaint = android.graphics.Paint().apply {
            color = textColor.toArgb()
            textSize = with(density) { 10.sp.toPx() }
            isAntiAlias = true
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw X-axis labels with improved time range handling
        val timeSteps = 4
        for (i in 0..timeSteps) {
            val x = chartLeft + (chartWidth * i / timeSteps)
            val timeValue = (displayTimeRange * i / timeSteps) / 1000 // Convert to seconds

            // X-axis label with minute format
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
                    chartBottom + with(density) { 16.dp.toPx() },
                    xAxisTextPaint
                )
            }
        }

        // Draw power line using relative timestamp
        if (data.size >= 2) {
            val path = Path()
            data.forEachIndexed { index, point ->
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val y = chartBottom - (chartHeight * (point.power - minPower) / adjustedPowerRange.coerceAtLeast(0.01f))

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Draw data points using relative timestamp
        data.forEach { point ->
            val relativeTimestamp = point.timestamp - minTime
            val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
            val y = chartBottom - (chartHeight * (point.power - minPower) / adjustedPowerRange.coerceAtLeast(0.01f))

            val pointRadius = when {
                data.size > 30 -> 1.dp.toPx() // use small points for large datasets
                else -> 2.dp.toPx() // use larger points for small datasets
            }

            drawCircle(
                color = lineColor,
                radius = pointRadius,
                center = Offset(x, y)
            )
        }
    }
}
