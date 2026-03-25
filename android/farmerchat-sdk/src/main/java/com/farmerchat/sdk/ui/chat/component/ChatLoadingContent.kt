package com.farmerchat.sdk.ui.chat.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors

@Composable
internal fun ChatLoadingContent(modifier: Modifier = Modifier) {
    val extColors = LocalSdkExtendedColors.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val avatarEmoji = config?.aiAvatarEmoji ?: "🌱"
    val primaryColor = MaterialTheme.colorScheme.primary
    val bubbleShape = RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Typing bubble — matches AiResponseBubble style
        Surface(
            shape = bubbleShape,
            color = extColors.aiBubbleBackground,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                // Header row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarEmoji, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = config?.chatTitle ?: "FarmerChat AI",
                        style = MaterialTheme.typography.labelSmall,
                        color = primaryColor,
                        fontSize = 11.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                // Bouncing dots
                val infiniteTransition = rememberInfiniteTransition(label = "typing")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        val offsetY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = -7f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(380, delayMillis = index * 120, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot_$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(y = offsetY.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.55f + index * 0.15f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ShimmerBlock(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val extColors = LocalSdkExtendedColors.current

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            extColors.aiBubbleBackground,
            extColors.aiBubbleBackground.copy(alpha = 0.5f),
            extColors.aiBubbleBackground
        ),
        start = Offset(shimmerX, 0f),
        end = Offset(shimmerX + 400f, 0f)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI bubble shimmer — LEFT aligned
        Column(
            modifier = Modifier
                .widthIn(max = screenWidth * 0.85f)
                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                .background(shimmerBrush)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.width(80.dp).height(10.dp).clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)))
            Spacer(Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth().height(11.dp).clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            Box(modifier = Modifier.fillMaxWidth().height(11.dp).clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(11.dp).clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
        }

        // User bubble shimmer — RIGHT aligned
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 100.dp, max = screenWidth * 0.55f)
                    .height(42.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
