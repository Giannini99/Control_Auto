package br.com.sensorauto.ui.screen.config.bubble

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import br.com.sensorauto.ui.theme.GreenSuccess
import kotlin.math.min

@Composable
fun BubbleLevelWidget(
    pitchDeg: Float,
    rollDeg: Float,
    isLevel: Boolean,
    modifier: Modifier = Modifier
) {
    val bubbleColor = if (isLevel) GreenSuccess else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val crossColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)

    val animatedX = remember { Animatable(0f) }
    val animatedY = remember { Animatable(0f) }

    LaunchedEffect(rollDeg, pitchDeg) {
        val maxAngle = 30f
        val targetX = (rollDeg / maxAngle).coerceIn(-1f, 1f)
        val targetY = (pitchDeg / maxAngle).coerceIn(-1f, 1f)

        animatedX.snapTo(targetX)
        animatedY.snapTo(targetY)
    }

    Box(
        modifier = modifier.size(260.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 16.dp.toPx()

            drawCircle(
                color = trackColor,
                radius = radius,
                center = center,
                style = Stroke(width = 3.dp.toPx())
            )

            drawCircle(
                color = trackColor.copy(alpha = 0.2f),
                radius = radius * 0.25f,
                center = center
            )
            drawCircle(
                color = bubbleColor.copy(alpha = 0.4f),
                radius = radius * 0.25f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            drawLine(
                color = crossColor,
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = crossColor,
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1.dp.toPx()
            )
            val maxOffsetPx = radius * 0.7f
            val bubbleX = center.x + animatedX.value * maxOffsetPx
            val bubbleY = center.y + animatedY.value * maxOffsetPx
            val bubbleRadius = 24.dp.toPx()

            drawCircle(
                color = bubbleColor,
                radius = bubbleRadius,
                center = Offset(bubbleX, bubbleY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = bubbleRadius * 0.3f,
                center = Offset(bubbleX - bubbleRadius * 0.3f, bubbleY - bubbleRadius * 0.3f)
            )
        }
    }
}
