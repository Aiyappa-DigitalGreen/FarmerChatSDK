package com.farmerchat.sdk.domain.model.conversation

import com.google.gson.annotations.SerializedName

data class NewConversationRequest(
    @SerializedName("user_id") val user_id: String,
    @SerializedName("content_provider_id") val content_provider_id: String?
)

data class NewConversationResponse(
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("message") val message: String,
    @SerializedName("show_popup") val show_popup: Boolean
)
