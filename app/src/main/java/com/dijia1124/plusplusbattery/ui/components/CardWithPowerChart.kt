package com.dijia1124.plusplusbattery.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
    powerData: List<PowerDataPoint>,
    onResetData: () -> Unit = {}
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            NormalBatteryCard(info)
            Spacer(modifier = Modifier.weight(1f))
            if (expanded) {
                IconButton(onClick = onResetData, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Reset Chart Data"
                    )
                }
            }
            IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }
        if (expanded) {
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

        // Handle empty data case - show empty chart with grid
        val isEmpty = data.isEmpty()
        val displayData = if (isEmpty) {
            // Create dummy data point for empty state
            listOf(PowerDataPoint(0L, 0f, 0f))
        } else {
            data
        }

        // Calculate data ranges
        val minTime = displayData.first().timestamp
        val maxTime = if (isEmpty) 30000L else displayData.last().timestamp
        val rawMaxPower = if (isEmpty) 1f else displayData.maxOfOrNull { it.power } ?: 1f
        // Nice tick step (always 5 ticks: 0..4)
        val stepCandidates = listOf(
            0.05f, 0.1f, 0.2f, 0.25f, 0.5f,
            1f, 2f, 2.5f, 5f, 10f, 20f, 25f, 50f, 100f
        )
        val desiredStepBase = (rawMaxPower / 4f).coerceAtLeast(0.01f)
        val powerStep = stepCandidates.firstOrNull { it >= desiredStepBase } ?: stepCandidates.last()
        val maxPower = powerStep * 4f
        // We intentionally allow maxPower > rawMaxPower to land on round ticks
        val rawMinTemp = data.minOfOrNull { it.temperature } ?: 0f
        val rawMaxTemp = data.maxOfOrNull { it.temperature } ?: 1f

        // Handle temperature range
        val tempMargin = kotlin.math.max((rawMaxTemp - rawMinTemp) * 0.1f, 5f)
        val minTemp = kotlin.math.floor(rawMinTemp - tempMargin).toFloat()
        val maxTemp = kotlin.math.ceil(rawMaxTemp + tempMargin).toFloat()
        val adjustedTempRange = maxTemp - minTemp

        // Handle time range
        val rawTimeRange = maxTime - minTime
        val displayTimeRange = if (rawTimeRange < 30000) {
            kotlin.math.max(rawTimeRange, 30000)
        } else rawTimeRange

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
            val powerValue = maxPower - powerStep * i
            val text = if (powerStep < 1f) String.format("%.1f", powerValue) else powerValue.toInt().toString()
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
            val powerValue = maxPower - powerStep * i
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
                val base = if (powerStep < 1f) String.format("%.1f", powerValue) else powerValue.toInt().toString()
                val text = "$base W"
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

        // Use actual data timestamps for X-axis ticks
        val tickIndices = if (isEmpty) {
            emptyList()
        } else {
            when {
                data.size < 30 -> {
                    // Show only last tick to prevent overlap in early stages
                    listOf(data.lastIndex)
                }
                else -> {
                    // Show 5 evenly spaced ticks once we have enough data
                    val maxTicks = 5
                    val step = (data.size - 1).toFloat() / (maxTicks - 1)
                    (0 until maxTicks).map { i ->
                        when (i) {
                            maxTicks - 1 -> data.lastIndex
                            else -> (i * step).toInt()
                        }
                    }
                }
            }
        }

        // Draw X-axis labels (only if not empty)
        if (!isEmpty) {
            tickIndices.forEach { index ->
                val point = data[index]
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val elapsedSeconds = relativeTimestamp / 1000

                drawIntoCanvas { canvas ->
                    val text = when {
                        elapsedSeconds >= 60 -> {
                            val minutes = (elapsedSeconds / 60).toInt()
                            val seconds = (elapsedSeconds % 60).toInt()
                            if (seconds == 0) "${minutes}m" else "${minutes}m${seconds}s"
                        }
                        else -> "${elapsedSeconds}s"
                    }
                    canvas.nativeCanvas.drawText(
                        text,
                        x,
                        chartBottom + with(density) { 20.dp.toPx() },
                        xAxisTextPaint
                    )
                }
            }
        }

        // Draw power line (only if not empty and has multiple points)
        if (!isEmpty && data.size >= 2) {
            val powerPath = Path()
            data.forEachIndexed { index, point ->
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val y = chartBottom - (chartHeight * point.power / maxPower.coerceAtLeast(0.0001f))

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

        // Draw temperature line (only if not empty and has multiple points)
        if (!isEmpty && data.size >= 2) {
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

        // Draw power data points (only if not empty)
        if (!isEmpty) {
            data.forEach { point ->
                val relativeTimestamp = point.timestamp - minTime
                val x = chartLeft + (chartWidth * relativeTimestamp / displayTimeRange.coerceAtLeast(1))
                val y = chartBottom - (chartHeight * point.power / maxPower.coerceAtLeast(0.0001f))

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
        }

        // Draw temperature data points (only if not empty)
        if (!isEmpty) {
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
}
