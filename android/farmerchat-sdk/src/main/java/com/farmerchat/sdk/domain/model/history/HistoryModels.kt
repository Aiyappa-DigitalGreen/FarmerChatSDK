package com.farmerchat.sdk.domain.model.history

import com.google.gson.annotations.SerializedName

// API returns a plain JSON array — no wrapper object, no pagination fields.
// Use List<ConversationListItem> directly as the Retrofit response type.

data class ConversationListItem(
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("conversation_title") val conversation_title: String?,
    @SerializedName("created_on") val created_on: String,
    @SerializedName("message_type") val message_type: String?,
    @SerializedName("grouping") val grouping: String?,
    @SerializedName("content_provider_logo") val content_provider_logo: String?
)
