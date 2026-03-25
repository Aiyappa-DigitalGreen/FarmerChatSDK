package com.farmerchat.sdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.farmerchat.sdk.R
import com.farmerchat.sdk.di.SdkKoinHolder
import com.farmerchat.sdk.ui.FarmerChatNavHost
import com.farmerchat.sdk.ui.theme.SdkGreen800
import com.farmerchat.sdk.ui.theme.SdkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext

private const val EXIT_ANIM_DURATION_MS = 300L

/**
 * A pre-built Floating Action Button that opens the FarmerChat chatbot as a
 * full-screen overlay that slides up from the bottom.
 */
@Composable
fun FarmerChatFab(
    extended: Boolean = true,
    label: String? = null,
    conversationId: String? = null,
    fabBackgroundColor: Color? = null,
    fabContentColor: Color? = null,
    icon: ImageVector? = null,
    iconPainter: Painter? = null,
    modifier: Modifier = Modifier
) {
    val config = runCatching { FarmerChatSdk.config }.getOrNull()

    val displayLabel = label ?: (config?.fabLabel ?: "FarmerChat")
    val fabBg = fabBackgroundColor
        ?: config?.fabBackgroundColor?.let { Color(it) }
        ?: config?.primaryColor?.let { Color(it) }
        ?: SdkGreen800
    val fabFg = fabContentColor
        ?: config?.fabContentColor?.let { Color(it) }
        ?: Color.White
    val fabIcon = icon ?: config?.fabIcon

    var showChat by remember { mutableStateOf(false) }
    var animateIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(animateIn) {
        if (!animateIn && showChat) {
            delay(EXIT_ANIM_DURATION_MS)
            showChat = false
        }
    }

    // Pulse animation for the FAB
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // ── FAB button ──────────────────────────────────────────────────────────────
    if (extended) {
        ExtendedFloatingActionButton(
            onClick = {
                if (FarmerChatSdk.isInitialized()) {
                    scope.launch {
                        FarmerChatSdk.ensureTokensInternal()
                        showChat = true
                    }
                }
            },
            containerColor = fabBg,
            contentColor = fabFg,
            shape = RoundedCornerShape(18.dp),
            modifier = modifier
        ) {
            when {
                iconPainter != null -> Icon(painter = iconPainter, contentDescription = displayLabel, modifier = Modifier.size(22.dp))
                fabIcon != null -> Icon(imageVector = fabIcon, contentDescription = displayLabel, modifier = Modifier.size(20.dp))
                else -> Image(
                    painter = painterResource(R.drawable.sdk_logo),
                    contentDescription = displayLabel,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(text = displayLabel, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        // Circular FAB with pulsing ring
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            // Pulse ring drawn behind the FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(pulseScale)
                    .background(
                        color = fabBg.copy(alpha = pulseAlpha),
                        shape = CircleShape
                    )
            )
            FloatingActionButton(
                onClick = {
                    if (FarmerChatSdk.isInitialized()) {
                        scope.launch {
                            FarmerChatSdk.ensureTokensInternal()
                            showChat = true
                        }
                    }
                },
                containerColor = fabBg,
                contentColor = fabFg,
                shape = CircleShape
            ) {
                when {
                    iconPainter != null -> Icon(painter = iconPainter, contentDescription = displayLabel, modifier = Modifier.size(28.dp))
                    fabIcon != null -> Icon(imageVector = fabIcon, contentDescription = displayLabel)
                    else -> Image(
                        painter = painterResource(R.drawable.sdk_logo),
                        contentDescription = displayLabel,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // ── Full-screen slide-up overlay ────────────────────────────────────────────
    if (showChat) {
        Dialog(
            onDismissRequest = { animateIn = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false
            )
        ) {
            LaunchedEffect(Unit) { animateIn = true }

            AnimatedVisibility(
                visible = animateIn,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = 350)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(durationMillis = EXIT_ANIM_DURATION_MS.toInt())
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    KoinContext(context = SdkKoinHolder.koin) {
                        SdkTheme {
                            FarmerChatNavHost(
                                startConversationId = conversationId,
                                onClose = { animateIn = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
