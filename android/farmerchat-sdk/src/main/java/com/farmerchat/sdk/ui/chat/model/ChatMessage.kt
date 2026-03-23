package com.farmerchat.sdk.ui.chat.model

import android.net.Uri
import java.util.UUID

sealed class ChatMessage {
    abstract val id: String

    data class UserMessage(
        val text: String,
        val imageUri: Uri? = null,
        val audioUri: Uri? = null,
        override val id: String = UUID.randomUUID().toString()
    ) : ChatMessage()

    data class AiResponse(
        val text: String,
        val followUpQuestions: List<String>? = null,
        val followUpQuestionIds: List<String>? = null,
        override val id: String = UUID.randomUUID().toString(),
        val isPreGenerated: Boolean = false,
        val messageId: String? = null
    ) : ChatMessage()

    data class LoadingPlaceholder(
        override val id: String = UUID.randomUUID().toString()
    ) : ChatMessage()
}
