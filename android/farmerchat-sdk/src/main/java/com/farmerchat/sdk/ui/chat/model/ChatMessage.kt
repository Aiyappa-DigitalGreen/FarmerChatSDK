package com.farmerchat.sdk.ui.chat.model

import android.net.Uri
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

private val TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
internal fun nowTimeString() = try { LocalTime.now().format(TIME_FMT) } catch (e: Exception) { "" }

sealed class ChatMessage {
    abstract val id: String
    /** Time captured at message creation — never re-read from clock at render time. */
    abstract val timeString: String

    data class UserMessage(
        val text: String,
        val imageUri: Uri? = null,
        val audioUri: Uri? = null,
        override val id: String = UUID.randomUUID().toString(),
        override val timeString: String = nowTimeString()
    ) : ChatMessage()

    data class AiResponse(
        val text: String,
        val followUpQuestions: List<String>? = null,
        val followUpQuestionIds: List<String>? = null,
        override val id: String = UUID.randomUUID().toString(),
        val isPreGenerated: Boolean = false,
        val messageId: String? = null,
        override val timeString: String = nowTimeString()
    ) : ChatMessage()

    data class LoadingPlaceholder(
        override val id: String = UUID.randomUUID().toString(),
        override val timeString: String = ""
    ) : ChatMessage()
}
