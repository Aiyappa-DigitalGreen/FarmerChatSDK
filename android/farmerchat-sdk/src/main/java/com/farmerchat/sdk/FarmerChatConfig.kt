package com.farmerchat.sdk

import com.farmerchat.sdk.api.SdkAnalyticsListener

/**
 * Configuration object for initializing the FarmerChat SDK.
 *
 * @param sdkApiKey   Your SDK API key issued by FarmerChat (format: fc_live_xxx or fc_test_xxx).
 *                    Sent as `X-SDK-Key` on every API request so the backend can associate
 *                    requests with your integration and apply per-app rate limits.
 * @param baseUrl     The base URL for the FarmerChat API.
 * @param contentProviderId Optional content provider ID for multi-tenant setups.
 * @param conversationId Optional initial conversation ID to open.
 * @param analyticsListener Optional listener for SDK analytics events.
 * @param primaryColor Primary color for the SDK UI as an ARGB Long (e.g. 0xFF2E7D32L for green).
 * @param fabLabel Label text shown on the FarmerChat floating action button.
 */
data class FarmerChatConfig(
    val sdkApiKey: String,
    val baseUrl: String,
    val contentProviderId: String? = null,
    val conversationId: String? = null,
    val analyticsListener: SdkAnalyticsListener? = null,
    val primaryColor: Long = 0xFF2E7D32L,
    val fabLabel: String = "Chat with FarmerChat"
)
