package com.farmerchat.sdk

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.farmerchat.sdk.di.SdkKoinHolder
import com.farmerchat.sdk.ui.FarmerChatNavHost
import com.farmerchat.sdk.ui.theme.SdkGreen800
import com.farmerchat.sdk.ui.theme.SdkTheme
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext

/**
 * A pre-built Floating Action Button that opens the FarmerChat chatbot
 * as an in-app bottom sheet — slides up from the bottom, overlaying the
 * current screen with a rounded chat window. No new Activity is launched.
 *
 * Usage:
 * ```kotlin
 * Scaffold(
 *     floatingActionButton = { FarmerChatFab() }
 * ) { ... }
 * ```
 *
 * @param extended        If true, shows the label alongside the icon.
 * @param label           Optional override for the FAB label.
 * @param conversationId  Optional specific conversation to open.
 * @param containerColor  Background color of the FAB.
 * @param contentColor    Icon/text color.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerChatFab(
    extended: Boolean = true,
    label: String? = null,
    conversationId: String? = null,
    containerColor: Color = SdkGreen800,
    contentColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val displayLabel = label
        ?: runCatching { FarmerChatSdk.config.fabLabel }.getOrDefault("FarmerChat")

    var showChat by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            containerColor = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Filled.Forum,
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
            containerColor = containerColor,
            contentColor = contentColor,
            shape = RoundedCornerShape(16.dp),
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.Filled.Forum,
                contentDescription = displayLabel
            )
        }
    }

    // ── Bottom sheet chat window ───────────────────────────────────────────────
    if (showChat) {
        ModalBottomSheet(
            onDismissRequest = { showChat = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            // 93% of screen height — leaves a sliver of the host app visible at the top
            modifier = Modifier.fillMaxHeight(0.93f),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            KoinContext(context = SdkKoinHolder.koin) {
                SdkTheme {
                    FarmerChatNavHost(
                        startConversationId = conversationId,
                        onClose = {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion { showChat = false }
                        }
                    )
                }
            }
        }
    }
}
