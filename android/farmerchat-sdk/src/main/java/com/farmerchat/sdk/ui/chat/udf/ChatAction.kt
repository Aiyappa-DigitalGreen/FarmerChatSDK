package com.farmerchat.sdk.ui.chat.udf

import android.net.Uri

sealed class ChatAction {

    data class InitializeWithPreGeneratedContent(
        val question: String,
        val answer: String?,
        val followUpQuestions: List<String>?
    ) : ChatAction()

    data class InitializeWithQuestion(
        val question: String,
        val transcriptionId: String? = null,
        val audioUri: Uri? = null
    ) : ChatAction()

    data class ReplacePreGeneratedWithQuestion(
        val question: String,
        val triggerInputType: String? = null
    ) : ChatAction()

    data class SendFollowUpQuestion(
        val question: String,
        val followUpQuestionId: String? = null,
        val transcriptionId: String? = null,
        val audioUri: Uri? = null
    ) : ChatAction()

    data class SendQuestionWithImage(
        val question: String,
        val imageUri: Uri
    ) : ChatAction()

    data class SendQuestionWithAudio(
        val question: String,
        val audioUri: Uri
    ) : ChatAction()

    /** Raw audio bytes (base64) → transcribe API → then send as text query */
    data class TranscribeAndSendAudio(
        val audioBase64: String,
        val audioFormat: String,
        val audioUri: Uri?
    ) : ChatAction()

    data class LoadChatHistory(
        val conversationId: String,
        val page: Int = 1
    ) : ChatAction()

    object RetryLastRequest : ChatAction()
    object ClearError : ChatAction()
    object ClearMessages : ChatAction()
    object SynthesiseAudio : ChatAction()
    object ClearAudioPlaybackUrl : ChatAction()

    data class SetAudioPlaying(val isPlaying: Boolean) : ChatAction()
}
