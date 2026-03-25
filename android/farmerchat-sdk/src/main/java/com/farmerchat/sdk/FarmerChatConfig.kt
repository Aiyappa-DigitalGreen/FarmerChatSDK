package com.farmerchat.sdk

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.ui.graphics.vector.ImageVector
import com.farmerchat.sdk.api.SdkAnalyticsListener

/**
 * Complete configuration for the FarmerChat SDK.
 * All fields except [sdkApiKey] and [baseUrl] are optional — defaults match
 * the standard FarmerChat green theme.
 *
 * Color values use the `0xAARRGGBB` Long format, e.g. `0xFF2E7D32L` for green.
 * Pass `null` for any color to fall back to the automatic theme default.
 *
 * ---
 * ## Quick-start (minimum required):
 * ```kotlin
 * FarmerChatConfig(
 *     sdkApiKey = "fc_live_your_key",
 *     baseUrl   = "https://your-api.com/"
 * )
 * ```
 *
 * ## Full customization example:
 * ```kotlin
 * FarmerChatConfig(
 *     sdkApiKey = "fc_live_your_key",
 *     baseUrl   = "https://your-api.com/",
 *
 *     // FAB
 *     fabLabel           = "Ask AI",
 *     fabIcon            = Icons.Filled.SmartToy,
 *     fabBackgroundColor = 0xFF1565C0L,
 *     fabContentColor    = 0xFFFFFFFFL,
 *
 *     // Top bar
 *     chatTitle                = "My Assistant",
 *     chatSubtitle             = "Powered by AI",
 *     topBarBackgroundColor    = 0xFF1565C0L,
 *     topBarTitleColor         = 0xFFFFFFFFL,
 *     topBarSubtitleColor      = 0xFFBBDEFBL,
 *     showHistoryButton        = true,
 *
 *     // Chat area
 *     primaryColor             = 0xFF1565C0L,
 *     chatBackgroundColor      = 0xFFF5F5F5L,
 *
 *     // User bubble
 *     userBubbleColor          = 0xFF1565C0L,
 *     userBubbleTextColor      = 0xFFFFFFFFL,
 *     showUserAvatar           = true,
 *
 *     // AI bubble
 *     aiBubbleColor            = 0xFFE3F2FDL,
 *     aiBubbleTextColor        = 0xFF0D1117L,
 *     aiBubbleElevation        = 2f,
 *     aiAvatarEmoji            = "🤖",
 *     aiAvatarBackgroundColor  = 0xFFBBDEFBL,
 *
 *     // Bubble shape & text
 *     bubbleCornerRadius       = 20f,
 *     messageFontSizeSp        = 15f,
 *
 *     // Input bar
 *     inputHintText            = "Type your question...",
 *     inputBarBackgroundColor  = 0xFFFFFFFFL,
 *     sendButtonColor          = 0xFF1565C0L,
 *
 *     // Follow-up questions
 *     followUpHeaderText       = "Suggested questions",
 *     showFollowUpHeaderIcon   = true,
 *     followUpCardBackgroundColor = 0xFFE3F2FDL,
 *     followUpTextColor        = 0xFF0D1117L,
 *     followUpButtonColor      = 0xFF1565C0L,
 *     followUpButtonTextColor  = 0xFFFFFFFFL,
 * )
 * ```
 */
data class FarmerChatConfig(
    // ── Required ─────────────────────────────────────────────────────────────
    val sdkApiKey: String,
    val baseUrl: String,

    // ── Identity ──────────────────────────────────────────────────────────────
    val contentProviderId: String? = null,
    val conversationId: String? = null,
    val analyticsListener: SdkAnalyticsListener? = null,

    // ── FAB ───────────────────────────────────────────────────────────────────
    /** Text label next to the FAB icon (extended FAB only). */
    val fabLabel: String = "Chat with FarmerChat",
    /** Icon shown on the FAB. Any Material ImageVector. Default: Forum icon. */
    val fabIcon: ImageVector = Icons.Filled.Forum,
    /** FAB background color (0xAARRGGBB). Defaults to [primaryColor]. */
    val fabBackgroundColor: Long? = null,
    /** FAB icon and label color. Default: white. */
    val fabContentColor: Long = 0xFFFFFFFFL,

    // ── Global theme ─────────────────────────────────────────────────────────
    /** Primary brand color — toolbar accents, active states, send button, etc. */
    val primaryColor: Long = 0xFF2E7D32L,

    // ── Chat screen ───────────────────────────────────────────────────────────
    /** Main title in the chat top bar. */
    val chatTitle: String = "FarmerChat",
    /** Subtitle below the title in the chat top bar. */
    val chatSubtitle: String = "AI Farm Assistant",
    /** Background of the full chat screen. Default: white. */
    val chatBackgroundColor: Long = 0xFFFFFFFFL,
    /** Show or hide the history (clock) button in the chat toolbar. */
    val showHistoryButton: Boolean = true,

    // ── Top bar ───────────────────────────────────────────────────────────────
    /** Top bar background. Null = uses surface color. */
    val topBarBackgroundColor: Long? = null,
    /** Title text color in top bar. Null = uses onSurface. */
    val topBarTitleColor: Long? = null,
    /** Subtitle text color in top bar. Null = uses primaryColor. */
    val topBarSubtitleColor: Long? = null,

    // ── User bubble ───────────────────────────────────────────────────────────
    /** Background color of the user's message bubbles. */
    val userBubbleColor: Long = 0xFF2E7D32L,
    /** Text color inside user message bubbles. */
    val userBubbleTextColor: Long = 0xFFFFFFFFL,
    /** Show or hide the person avatar next to user messages. */
    val showUserAvatar: Boolean = true,

    // ── AI bubble ─────────────────────────────────────────────────────────────
    /** Background color of AI response bubbles. */
    val aiBubbleColor: Long = 0xFFF1F8E9L,
    /** Text color inside AI response bubbles. */
    val aiBubbleTextColor: Long = 0xFF1C1B1FL,
    /** Shadow/elevation of AI response bubbles in dp. */
    val aiBubbleElevation: Float = 1f,
    /** Emoji shown in the AI avatar circle. */
    val aiAvatarEmoji: String = "🌱",
    /** Background color of the AI avatar circle. Null = primaryContainer. */
    val aiAvatarBackgroundColor: Long? = null,

    // ── Bubble shape & typography ─────────────────────────────────────────────
    /**
     * Corner radius applied to ALL bubble corners in dp.
     * The "pointer" corner (top-start on AI, top-end on user) is always 4dp
     * regardless of this value, to preserve the chat-bubble look.
     */
    val bubbleCornerRadius: Float = 16f,
    /** Font size for all message text (user + AI) in sp. */
    val messageFontSizeSp: Float = 14f,

    // ── Input bar ─────────────────────────────────────────────────────────────
    /** Placeholder hint text inside the text input field. */
    val inputHintText: String = "Ask about your crops...",
    /** Background of the entire input bar area. Null = uses surface color. */
    val inputBarBackgroundColor: Long? = null,
    /** Background color of the send button. Null = uses primaryColor. */
    val sendButtonColor: Long? = null,

    // ── Follow-up questions ───────────────────────────────────────────────────
    /** Header text above the follow-up question cards. */
    val followUpHeaderText: String = "Related questions",
    /** Show or hide the lightbulb icon next to the follow-up header. */
    val showFollowUpHeaderIcon: Boolean = true,
    /** Background color of each follow-up question card. Null = surfaceVariant. */
    val followUpCardBackgroundColor: Long? = null,
    /** Text color of follow-up question text. Null = onSurface. */
    val followUpTextColor: Long? = null,
    /** Background color of the "Ask" button on each card. Null = primaryColor. */
    val followUpButtonColor: Long? = null,
    /** Text color of the "Ask" button. Default: white. */
    val followUpButtonTextColor: Long = 0xFFFFFFFFL,

    // ── Weather widget (optional) ─────────────────────────────────────────────
    /** Weather summary shown in chat header, e.g. "28°C Sunny". Null = widget hidden. */
    val weatherTemp: String? = null,
    /** Location text below weather, e.g. "Coorg, Karnataka". */
    val weatherLocation: String? = null,
    /** Active crop chip label, e.g. "Rice". */
    val cropName: String? = null,

    // ── Language selection ────────────────────────────────────────────────────
    /** When true, a language selection screen is shown before the chat on first launch. */
    val showLanguageSelection: Boolean = false
)
