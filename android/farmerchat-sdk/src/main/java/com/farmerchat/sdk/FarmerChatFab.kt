package com.farmerchat.sdk

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
 * full-screen overlay that slides up from the bottom — same feel as a bottom
 * sheet but covers the full screen like a new page.
 *
 * All visual defaults (colors, icon, label) are read from [FarmerChatConfig]
 * set during [FarmerChatSdk.initialize], but can be overridden per-FAB.
 *
 * @param extended           If true, shows the label alongside the icon.
 * @param label              Override for the FAB label (defaults to config value).
 * @param conversationId     Open a specific existing conversation.
 * @param fabBackgroundColor FAB background color (defaults to config value).
 * @param fabContentColor    FAB icon/text color (defaults to config value).
 * @param icon               FAB icon (defaults to config value).
 */
@Composable
fun FarmerChatFab(
    extended: Boolean = true,
    label: String? = null,
    conversationId: String? = null,
    fabBackgroundColor: Color? = null,
    fabContentColor: Color? = null,
    icon: ImageVector? = null,
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
    val fabIcon = icon ?: config?.fabIcon ?: Icons.Filled.Forum

    var showChat by remember { mutableStateOf(false) }
    var animateIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // When animateIn flips to false, wait for the exit animation then close the Dialog.
    LaunchedEffect(animateIn) {
        if (!animateIn && showChat) {
            delay(EXIT_ANIM_DURATION_MS)
            showChat = false
        }
    }

    // ── FAB button ─────────────────────────────────────────────────────────────
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
            contentColor   = fabFg,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            Icon(
                imageVector = fabIcon,
                contentDescription = displayLabel,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = displayLabel,
                style = MaterialTheme.typography.labelLarge
            )
        }
    } else {
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
            contentColor   = fabFg,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            Icon(imageVector = fabIcon, contentDescription = displayLabel)
        }
    }

    // ── Full-screen slide-up overlay ────────────────────────────────────────────
    if (showChat) {
        Dialog(
            // Back press: trigger exit animation; LaunchedEffect closes Dialog after delay
            onDismissRequest = { animateIn = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                decorFitsSystemWindows = false
            )
        ) {
            // Trigger slide-in on first composition
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
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    KoinContext(context = SdkKoinHolder.koin) {
                        SdkTheme {
                            FarmerChatNavHost(
                                startConversationId = conversationId,
                                // Close: trigger exit animation; LaunchedEffect handles Dialog dismissal
                                onClose = { animateIn = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
