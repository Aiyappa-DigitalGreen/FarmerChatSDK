package com.farmerchat.sdk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.ui.graphics.vector.ImageVector
import com.farmerchat.sdk.api.SdkAnalyticsListener

/**
 * Configuration object for initializing the FarmerChat SDK.
 *
 * ## Required
 * @param sdkApiKey   Your SDK API key (format: fc_live_xxx or fc_test_xxx).
 * @param baseUrl     The base URL for the FarmerChat API.
 *
 * ## Optional — Identity
 * @param contentProviderId Optional content provider ID for multi-tenant setups.
 * @param conversationId    Optional initial conversation ID to open.
 * @param analyticsListener Optional listener for SDK analytics events.
 *
 * ## Optional — FAB Appearance
 * @param fabLabel           Label text shown next to the FAB icon (extended mode).
 * @param fabIcon            Icon shown on the FAB. Defaults to a chat/forum icon.
 * @param fabBackgroundColor Background color of the FAB as 0xAARRGGBB Long.
 *                           Defaults to [primaryColor].
 * @param fabContentColor    Icon and label color on the FAB. Defaults to white.
 *
 * ## Optional — Theme Colors (0xAARRGGBB Long values)
 * @param primaryColor       Primary brand color — used in toolbar, active elements, etc.
 *                           Default: `0xFF2E7D32` (FarmerChat green).
 * @param userBubbleColor    Background color of the user's chat bubbles.
 * @param userBubbleTextColor Text color inside user chat bubbles.
 * @param aiBubbleColor      Background color of AI response bubbles.
 * @param aiBubbleTextColor  Text color inside AI response bubbles.
 *
 * ## Optional — Chat Screen Text
 * @param chatTitle    Title shown in the chat screen top bar. Default: "FarmerChat".
 * @param chatSubtitle Subtitle shown below the title. Default: "AI Farm Assistant".
 */
data class FarmerChatConfig(
    val sdkApiKey: String,
    val baseUrl: String,
    val contentProviderId: String? = null,
    val conversationId: String? = null,
    val analyticsListener: SdkAnalyticsListener? = null,

    // FAB
    val fabLabel: String = "Chat with FarmerChat",
    val fabIcon: ImageVector = Icons.Filled.Forum,
    val fabBackgroundColor: Long? = null,   // null → falls back to primaryColor
    val fabContentColor: Long = 0xFFFFFFFFL,

    // Theme colors
    val primaryColor: Long = 0xFF2E7D32L,
    val userBubbleColor: Long = 0xFF2E7D32L,
    val userBubbleTextColor: Long = 0xFFFFFFFFL,
    val aiBubbleColor: Long = 0xFFF1F8E9L,
    val aiBubbleTextColor: Long = 0xFF1C1B1FL,

    // Chat screen text
    val chatTitle: String = "FarmerChat",
    val chatSubtitle: String = "AI Farm Assistant"
)
