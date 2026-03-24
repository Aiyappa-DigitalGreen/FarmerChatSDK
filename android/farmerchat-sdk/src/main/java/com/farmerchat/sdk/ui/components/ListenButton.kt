package com.farmerchat.sdk.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ListenButton(
    isLoading: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    when {
        isLoading -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(primaryColor.copy(alpha = 0.08f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(13.dp),
                    strokeWidth = 1.5.dp,
                    color = primaryColor
                )
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.labelSmall,
                    color = primaryColor,
                    fontSize = 11.sp
                )
            }
        }

        isPlaying -> {
            val infiniteTransition = rememberInfiniteTransition(label = "wave")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(primaryColor.copy(alpha = 0.10f))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                // Animated sound bars
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    listOf(0, 1, 2, 3).forEach { i ->
                        val scaleY by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(
                                    durationMillis = 350,
                                    delayMillis = i * 80,
                                    easing = FastOutSlowInEasing
                                ),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "bar_$i"
                        )
                        Box(
                            modifier = Modifier
                                .width(2.5.dp)
                                .height((12.dp * scaleY).coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(2.dp))
                                .background(primaryColor)
                        )
                    }
                }
                Spacer(Modifier.width(2.dp))
                Text(
                    text = "Stop",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                )
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop audio",
                    tint = primaryColor,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        else -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(primaryColor.copy(alpha = 0.07f))
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Listen to response",
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Listen",
                    style = MaterialTheme.typography.labelMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                )
            }
        }
    }
}
