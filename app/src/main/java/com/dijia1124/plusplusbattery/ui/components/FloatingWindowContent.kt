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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

@Composable
fun FloatingWindowContent(
    text: String,
    alpha: Float,
    size: Float,
    textColor: Color,
    backgroundColor: Color,
    textShadowEnabled: Boolean,
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
                shadow = Shadow(
                    color = textColor.copy(alpha = 0.5f),
                    offset = Offset(2f, 2f),
                    blurRadius = 4f
                )
            )
        } else {
            MaterialTheme.typography.bodyLarge.copy(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize * size,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * size
            )
        }
        Text(
            text = text,
            color = textColor,
            style = textStyle
        )
    }
}
