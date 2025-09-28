package com.dijia1124.plusplusbattery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

@Composable
fun FloatingWindowContent(
    text: String,
    alpha: Float,
    size: Float,
    fontWeight: Int,
    textColor: Color,
    backgroundColor: Color,
    textShadowEnabled: Boolean,
    textStrokeEnabled: Boolean,
    onDrag: (Offset) -> Unit
) {
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount)
                }
            }
            .background(
                color = backgroundColor.copy(alpha = alpha),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        val textStyle = if (textShadowEnabled) {
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * size,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * size,
                fontWeight = FontWeight(fontWeight),
                shadow = Shadow(
                    color = textColor.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                ),
                fontFeatureSettings = "tnum"
            )
        } else {
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * size,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * size,
                fontWeight = FontWeight(fontWeight),
                fontFeatureSettings = "tnum"
            )
        }
        if (textStrokeEnabled) {
            StrokedText(
                text = text,
                color = textColor,
                style = textStyle
            )
        } else {
            Text(
                text = text,
                color = textColor,
                style = textStyle
            )
        }
    }
}

@Composable
fun StrokedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    style: TextStyle,
    strokeWidth: Float = 4f
) {
    // Calculate luminance to determine if the color is light or dark
    val luminance = sqrt(0.299 * color.red * color.red + 0.587 * color.green * color.green + 0.114 * color.blue * color.blue)
    val strokeColor = if (luminance > 0.5) Color.Black else Color.White

    Box(modifier) {
        Text(
            text = text,
            color = strokeColor,
            style = style.copy(
                drawStyle = Stroke(width = strokeWidth)
            )
        )
        Text(
            text = text,
            color = color,
            style = style
        )
    }
}
