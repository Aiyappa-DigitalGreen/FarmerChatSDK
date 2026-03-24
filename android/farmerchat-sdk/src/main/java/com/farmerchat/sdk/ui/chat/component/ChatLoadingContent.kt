package com.farmerchat.sdk.ui.chat.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
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
    val bubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        // Gradient avatar
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(3.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.18f))
                .clip(CircleShape)
                .background(Brush.linearGradient(colors = listOf(primaryColor.copy(alpha = 0.85f), primaryColor.copy(alpha = 0.55f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(text = avatarEmoji, fontSize = 18.sp)
        }

        Spacer(Modifier.width(10.dp))

        // Typing bubble
        Surface(
            shape = bubbleShape,
            color = extColors.aiBubbleBackground,
            shadowElevation = 6.dp,
            modifier = Modifier.border(
                0.5.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                bubbleShape
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "typing")
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
                            .background(primaryColor.copy(alpha = 0.65f + index * 0.1f))
                    )
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

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier.size(38.dp).clip(CircleShape).background(shimmerBrush)
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .widthIn(max = screenWidth * 0.72f)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 5.dp, bottomEnd = 20.dp))
                    .background(shimmerBrush)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(13.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(13.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                )
                Box(
                    modifier = Modifier.fillMaxWidth(0.55f).height(13.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                )
            }
        }

        // User bubble shimmer — RIGHT aligned
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .widthIn(min = 100.dp, max = screenWidth * 0.55f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 5.dp))
                    .background(shimmerBrush)
            )
        }
    }
}
