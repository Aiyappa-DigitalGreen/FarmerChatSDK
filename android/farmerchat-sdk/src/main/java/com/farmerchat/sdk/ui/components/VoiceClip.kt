package com.farmerchat.sdk.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

private const val BAR_COUNT = 40

@Composable
internal fun VoiceClip(
    isAnimating: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val animationOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_offset"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = 4.dp.toPx()
        val barSpacing = (canvasWidth - barWidth * BAR_COUNT) / (BAR_COUNT + 1)
        val centerY = canvasHeight / 2f
        val maxBarHeight = canvasHeight * 0.85f

        for (i in 0 until BAR_COUNT) {
            val x = barSpacing * (i + 1) + barWidth * i + barWidth / 2f
            val barHeight = if (isAnimating) {
                val phase = (i.toFloat() / BAR_COUNT) * 2 * PI.toFloat()
                val rawSin = sin(animationOffset + phase)
                val normalized = abs(rawSin) * 0.7f + 0.3f
                // Add some variation per bar
                val variation = 0.5f + 0.5f * abs(sin(phase * 2.5f))
                maxBarHeight * normalized * variation
            } else {
                // Static low bars when not recording
                val staticHeight = maxBarHeight * (0.1f + 0.1f * abs(sin(i.toFloat() / BAR_COUNT * PI.toFloat())))
                staticHeight
            }

            drawLine(
                color = barColor,
                start = Offset(x, centerY - barHeight / 2f),
                end = Offset(x, centerY + barHeight / 2f),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
