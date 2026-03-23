package com.farmerchat.sdk.ui.chat.udf

import com.farmerchat.sdk.base.UiState
import com.farmerchat.sdk.ui.chat.model.ChatMessage

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val suggestedQuestions: List<String>? = null,
    val suggestedQuestionIds: List<String>? = null,
    val chatResponseState: UiState<String> = UiState.Idle,
    val errorMessage: String? = null,
    val failedMessageId: String? = null,
    val isLoading: Boolean = false,
    val isLoadingSynthesiseAudio: Boolean = false,
    val audioPlaybackUrl: String? = null,
    val isAudioPlaying: Boolean = false,
    val historyNextPage: Int? = null,
    val isInitialHistoryLoaded: Boolean = false,
    val readFullAdviceRequestedForMessageId: String? = null,
    val currentConversationId: String? = null
)
