package com.farmerchat.sdk.ui.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.UiState
import com.farmerchat.sdk.data.repository.ConversationRepository
import com.farmerchat.sdk.domain.model.chat.ConversationChatHistoryMessageItem
import com.farmerchat.sdk.domain.usecase.ChatUseCase
import com.farmerchat.sdk.preference.SdkPreferenceManager
import com.farmerchat.sdk.ui.chat.model.ChatMessage
import com.farmerchat.sdk.ui.chat.udf.ChatAction
import com.farmerchat.sdk.ui.chat.udf.ChatState
import com.farmerchat.sdk.utils.ImageUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val TAG = "ChatViewModel"

internal class ChatViewModel(
    private val chatUseCase: ChatUseCase,
    private val conversationRepository: ConversationRepository,
    private val preferenceManager: SdkPreferenceManager,
    private val context: Context,
    private val initialConversationId: String?
) : ViewModel() {

    private val _state = MutableStateFlow(
        ChatState(currentConversationId = initialConversationId)
    )
    val state: StateFlow<ChatState> = _state.asStateFlow()

    // Holds the last action for retry support
    private var lastAction: ChatAction? = null

    init {
        if (!initialConversationId.isNullOrEmpty()) {
            dispatch(ChatAction.LoadChatHistory(initialConversationId))
        }
    }

    fun dispatch(action: ChatAction) {
        when (action) {
            is ChatAction.InitializeWithPreGeneratedContent -> handleInitializePreGenerated(action)
            is ChatAction.InitializeWithQuestion -> handleInitializeWithQuestion(action)
            is ChatAction.ReplacePreGeneratedWithQuestion -> handleReplacePreGenerated(action)
            is ChatAction.SendFollowUpQuestion -> handleSendFollowUp(action)
            is ChatAction.SendQuestionWithImage -> handleSendImage(action)
            is ChatAction.SendQuestionWithAudio -> handleSendAudio(action)
            is ChatAction.TranscribeAndSendAudio -> handleTranscribeAndSend(action)
            is ChatAction.LoadChatHistory -> handleLoadHistory(action)
            is ChatAction.RetryLastRequest -> lastAction?.let { dispatch(it) }
            is ChatAction.ClearError -> _state.update { it.copy(errorMessage = null, failedMessageId = null) }
            is ChatAction.ClearMessages -> _state.update { ChatState(currentConversationId = _state.value.currentConversationId) }
            is ChatAction.SynthesiseAudio -> handleSynthesiseAudio()
            is ChatAction.ClearAudioPlaybackUrl -> _state.update { it.copy(audioPlaybackUrl = null) }
            is ChatAction.SetAudioPlaying -> _state.update { it.copy(isAudioPlaying = action.isPlaying) }
        }
    }

    // ───── Entry point 1: Pre-generated content ─────

    private fun handleInitializePreGenerated(action: ChatAction.InitializeWithPreGeneratedContent) {
        val userMsg = ChatMessage.UserMessage(text = action.question)
        val aiMsg = ChatMessage.AiResponse(
            text = action.answer ?: "",
            followUpQuestions = action.followUpQuestions,
            isPreGenerated = true
        )
        _state.update {
            it.copy(
                messages = listOf(userMsg, aiMsg),
                suggestedQuestions = action.followUpQuestions,
                chatResponseState = UiState.Idle
            )
        }
    }

    // ───── Entry point 2: Initialize with a question (start fresh) ─────

    private fun handleInitializeWithQuestion(action: ChatAction.InitializeWithQuestion) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(
            text = action.question,
            audioUri = action.audioUri
        )
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        _state.update {
            it.copy(
                messages = listOf(userMsg, loadingMsg),
                chatResponseState = UiState.Loading,
                errorMessage = null,
                suggestedQuestions = null,
                suggestedQuestionIds = null
            )
        }
        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            sendTextQuery(
                query = action.question,
                conversationId = conversationId,
                triggeredInputType = if (action.audioUri != null) "audio" else "text",
                transcriptionId = action.transcriptionId,
                loadingMsgId = loadingMsg.id
            )
        }
    }

    // ───── Entry point 3: Replace pre-generated ─────

    private fun handleReplacePreGenerated(action: ChatAction.ReplacePreGeneratedWithQuestion) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(text = action.question)
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        _state.update {
            it.copy(
                messages = listOf(userMsg, loadingMsg),
                chatResponseState = UiState.Loading,
                errorMessage = null,
                suggestedQuestions = null,
                suggestedQuestionIds = null
            )
        }
        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            sendTextQuery(
                query = action.question,
                conversationId = conversationId,
                triggeredInputType = action.triggerInputType ?: "text",
                loadingMsgId = loadingMsg.id
            )
        }
    }

    // ───── Entry point 4: Follow-up question ─────

    private fun handleSendFollowUp(action: ChatAction.SendFollowUpQuestion) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(
            text = action.question,
            audioUri = action.audioUri
        )
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        appendMessages(userMsg, loadingMsg)
        // Clear previous follow-up suggestions immediately when a new question starts
        _state.update {
            it.copy(
                chatResponseState = UiState.Loading,
                errorMessage = null,
                suggestedQuestions = null,
                suggestedQuestionIds = null
            )
        }

        // Track follow-up click — API expects the question ID (UUID), not the text
        if (!action.followUpQuestionId.isNullOrEmpty()) {
            viewModelScope.launch {
                chatUseCase.trackFollowUpQuestionClick(action.followUpQuestionId).collect { }
            }
        }
        FarmerChatSdk.config.analyticsListener?.onFollowUpQuestionTapped(
            question = action.question,
            questionId = action.followUpQuestionId
        )

        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            sendTextQuery(
                query = action.question,
                conversationId = conversationId,
                triggeredInputType = if (action.audioUri != null) "audio" else "follow_up",
                transcriptionId = action.transcriptionId,
                loadingMsgId = loadingMsg.id
            )
        }
    }

    // ───── Image query ─────

    private fun handleSendImage(action: ChatAction.SendQuestionWithImage) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(text = action.question, imageUri = action.imageUri)
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        appendMessages(userMsg, loadingMsg)
        _state.update {
            it.copy(
                chatResponseState = UiState.Loading,
                errorMessage = null,
                suggestedQuestions = null,
                suggestedQuestionIds = null
            )
        }

        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            try {
                val imageBase64 = ImageUtils.getBase64FromUri(context, action.imageUri)
                val imageName = ImageUtils.generateUniqueImageName()
                val location = ImageUtils.getLocationFromExif(context, action.imageUri)

                FarmerChatSdk.config.analyticsListener?.onImageQuerySent(conversationId)

                chatUseCase.sendImageQuery(
                    conversationId = conversationId,
                    imageBase64 = imageBase64,
                    imageName = imageName,
                    query = action.question.ifBlank { null },
                    latitude = location?.first,
                    longitude = location?.second
                ).collect { result ->
                    when (result) {
                        is ApiResult.Success -> {
                            val response = result.data
                            var followUps: List<String>? = null
                            var followUpIds: List<String>? = null

                            val imgMsgId = response.message_id
                            if (imgMsgId.isNotEmpty()) {
                                chatUseCase.getFollowUpQuestions(imgMsgId).collect { fuResult ->
                                    if (fuResult is ApiResult.Success) {
                                        val questions = fuResult.data.questions
                                        followUps = questions?.mapNotNull { it.question }
                                        followUpIds = questions?.mapNotNull { it.follow_up_question_id }
                                    }
                                }
                            }
                            if (followUps.isNullOrEmpty()) {
                                followUps = response.follow_up_questions?.mapNotNull { it.question }
                                followUpIds = response.follow_up_questions?.mapNotNull { it.follow_up_question_id }
                            }

                            replaceLoadingWithResponse(
                                loadingMsgId = loadingMsg.id,
                                text = response.response,
                                followUpQuestions = followUps,
                                messageId = response.message_id
                            )
                            _state.update {
                                it.copy(
                                    suggestedQuestions = followUps,
                                    suggestedQuestionIds = followUpIds,
                                    chatResponseState = UiState.Idle
                                )
                            }
                        }
                        is ApiResult.Error -> handleApiError(result, loadingMsg.id)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Image processing failed", e)
                removeLoadingAndSetError(loadingMsg.id, e.localizedMessage ?: "Image processing failed")
            }
        }
    }

    // ───── Audio query ─────

    private fun handleSendAudio(action: ChatAction.SendQuestionWithAudio) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(text = action.question, audioUri = action.audioUri)
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        appendMessages(userMsg, loadingMsg)
        _state.update { it.copy(chatResponseState = UiState.Loading, errorMessage = null) }

        FarmerChatSdk.config.analyticsListener?.let { listener ->
            val convId = _state.value.currentConversationId ?: ""
            listener.onVoiceQuerySent(convId)
        }

        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            sendTextQuery(
                query = action.question,
                conversationId = conversationId,
                triggeredInputType = "audio",
                loadingMsgId = loadingMsg.id
            )
        }
    }

    // ───── Transcribe audio then send ─────

    private fun handleTranscribeAndSend(action: ChatAction.TranscribeAndSendAudio) {
        lastAction = action
        val userMsg = ChatMessage.UserMessage(text = "🎤 ...", audioUri = action.audioUri)
        val loadingMsg = ChatMessage.LoadingPlaceholder()
        appendMessages(userMsg, loadingMsg)
        _state.update {
            it.copy(
                chatResponseState = UiState.Loading,
                errorMessage = null,
                suggestedQuestions = null,
                suggestedQuestionIds = null
            )
        }

        viewModelScope.launch {
            val conversationId = ensureConversationId() ?: return@launch
            val messageRefId = UUID.randomUUID().toString()

            chatUseCase.transcribeAudio(
                conversationId = conversationId,
                audioBase64 = action.audioBase64,
                messageReferenceId = messageRefId,
                audioFormat = action.audioFormat
            ).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val transcription = result.data
                        val heardText = transcription.heard_input_query
                        if (heardText.isNullOrBlank()) {
                            removeLoadingAndSetError(loadingMsg.id, "Could not understand audio")
                            return@collect
                        }
                        // Update user bubble with real transcribed text
                        _state.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    if (msg.id == userMsg.id) {
                                        (msg as ChatMessage.UserMessage).copy(text = heardText)
                                    } else msg
                                }
                            )
                        }
                        sendTextQuery(
                            query = heardText,
                            conversationId = conversationId,
                            triggeredInputType = "audio",
                            transcriptionId = transcription.transcription_id,
                            loadingMsgId = loadingMsg.id
                        )
                    }
                    is ApiResult.Error -> handleApiError(result, loadingMsg.id)
                }
            }
        }
    }

    // ───── Load history ─────

    private fun handleLoadHistory(action: ChatAction.LoadChatHistory) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            // Ensure tokens before making the history API call
            try {
                FarmerChatSdk.ensureTokensInternal()
            } catch (e: Exception) {
                val msg = e.message ?: "Unable to load chat history. Please try again."
                _state.update { it.copy(isLoading = false, errorMessage = msg) }
                return@launch
            }
            chatUseCase.getChatHistory(action.conversationId, action.page).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        // API returns newest-first; reverse section order but keep items within
                        // each section in their original order (user query before AI response).
                        val historyMessages = convertHistoryToMessages(reverseHistorySections(result.data.data))
                        // Set follow-up suggestions from the last AI response in the loaded batch
                        val lastAi = historyMessages.filterIsInstance<ChatMessage.AiResponse>().lastOrNull()
                        _state.update { state ->
                            if (action.page == 1) {
                                state.copy(
                                    messages = historyMessages,
                                    isLoading = false,
                                    isInitialHistoryLoaded = true,
                                    currentConversationId = action.conversationId,
                                    suggestedQuestions = lastAi?.followUpQuestions,
                                    suggestedQuestionIds = lastAi?.followUpQuestionIds
                                )
                            } else {
                                state.copy(
                                    messages = historyMessages + state.messages,
                                    isLoading = false
                                )
                            }
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Load history failed: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = result.message ?: "Failed to load history"
                            )
                        }
                        FarmerChatSdk.config.analyticsListener?.onApiError(
                            result.apiName, result.code, result.message
                        )
                    }
                }
            }
        }
    }

    // ───── Audio synthesis ─────

    private fun handleSynthesiseAudio() {
        val lastAiMsg = _state.value.messages
            .filterIsInstance<ChatMessage.AiResponse>()
            .lastOrNull() ?: return

        val messageId = lastAiMsg.messageId ?: return
        val userId = preferenceManager.getUserId() ?: return
        val text = lastAiMsg.text

        FarmerChatSdk.config.analyticsListener?.onListenButtonTapped(messageId)

        _state.update { it.copy(isLoadingSynthesiseAudio = true) }
        viewModelScope.launch {
            chatUseCase.synthesiseAudio(messageId, text, userId).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val audioUrl = result.data.audio
                        _state.update {
                            it.copy(
                                isLoadingSynthesiseAudio = false,
                                audioPlaybackUrl = audioUrl
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        Log.e(TAG, "Synthesise audio failed: ${result.message}")
                        _state.update {
                            it.copy(
                                isLoadingSynthesiseAudio = false,
                                errorMessage = result.message
                            )
                        }
                        FarmerChatSdk.config.analyticsListener?.onApiError(
                            result.apiName, result.code, result.message
                        )
                    }
                }
            }
        }
    }

    // ───── Helpers ─────

    private suspend fun ensureConversationId(): String? {
        val current = _state.value.currentConversationId
        if (!current.isNullOrEmpty()) return current

        // Actively ensure a guest session exists (calls initialize_user if no tokens stored).
        // If the device has hit its guest-user limit or there is no internet, this throws
        // AuthInitException with a user-facing message.
        try {
            FarmerChatSdk.ensureTokensInternal()
        } catch (e: Exception) {
            Log.e(TAG, "Auth init failed: ${e.message}")
            val msg = e.message ?: "Unable to start a chat session. Please try again."
            _state.update {
                it.copy(
                    chatResponseState = UiState.Error(msg),
                    errorMessage = msg
                )
            }
            return null
        }

        val userId = preferenceManager.getUserId()
        if (userId == null) {
            val msg = "Unable to start a chat session. Please try again."
            _state.update {
                it.copy(chatResponseState = UiState.Error(msg), errorMessage = msg)
            }
            return null
        }

        Log.d(TAG, "ensureConversationId — userId=$userId accessToken=${preferenceManager.getAccessToken()?.take(10)}")

        return when (val result = chatUseCase.newConversation(
            userId = userId,
            contentProviderId = FarmerChatSdk.config.contentProviderId
        )) {
            is ApiResult.Success -> {
                val conversationId = result.data.conversation_id
                _state.update { it.copy(currentConversationId = conversationId) }
                FarmerChatSdk.config.analyticsListener?.onNewConversationCreated(conversationId)
                conversationId
            }
            is ApiResult.Error -> {
                Log.e(TAG, "New conversation failed: ${result.message}")
                _state.update {
                    it.copy(
                        errorMessage = result.message ?: "Failed to start conversation",
                        chatResponseState = UiState.Error(result.message)
                    )
                }
                FarmerChatSdk.config.analyticsListener?.onApiError(
                    result.apiName, result.code, result.message
                )
                null
            }
        }
    }

    private suspend fun sendTextQuery(
        query: String,
        conversationId: String,
        triggeredInputType: String,
        transcriptionId: String? = null,
        loadingMsgId: String
    ) {
        FarmerChatSdk.config.analyticsListener?.onTextQuerySent(query, conversationId, triggeredInputType)

        chatUseCase.sendTextQuery(
            query = query,
            conversationId = conversationId,
            messageId = UUID.randomUUID().toString(),
            triggeredInputType = triggeredInputType,
            transcriptionId = transcriptionId
        ).collect { result ->
            when (result) {
                is ApiResult.Success -> {
                    val response = result.data
                    var followUps: List<String>? = null
                    var followUpIds: List<String>? = null

                    // Always fetch from the dedicated follow-up endpoint when available.
                    // The text-prompt response's follow_up_questions field is unreliable.
                    val msgId = response.message_id
                    if (!msgId.isNullOrEmpty() && response.hide_follow_up_question != true) {
                        chatUseCase.getFollowUpQuestions(msgId).collect { fuResult ->
                            if (fuResult is ApiResult.Success) {
                                val questions = fuResult.data.questions
                                followUps = questions?.mapNotNull { it.question }
                                followUpIds = questions?.mapNotNull { it.follow_up_question_id }
                            }
                        }
                    }

                    // Fall back to inline follow-up questions from the text response
                    if (followUps.isNullOrEmpty()) {
                        followUps = response.follow_up_questions?.mapNotNull { it.question }
                        followUpIds = response.follow_up_questions?.mapNotNull { it.follow_up_question_id }
                    }

                    replaceLoadingWithResponse(
                        loadingMsgId = loadingMsgId,
                        text = response.response ?: response.translated_response ?: "",
                        followUpQuestions = followUps,
                        messageId = response.message_id
                    )
                    _state.update {
                        it.copy(
                            suggestedQuestions = followUps,
                            suggestedQuestionIds = followUpIds,
                            chatResponseState = UiState.Idle
                        )
                    }
                }
                is ApiResult.Error -> handleApiError(result, loadingMsgId)
            }
        }
    }

    private fun handleApiError(error: ApiResult.Error, loadingMsgId: String) {
        Log.e(TAG, "API error [${error.apiName}] code=${error.code}: ${error.message}")
        removeLoadingAndSetError(loadingMsgId, error.message ?: "An error occurred")
        FarmerChatSdk.config.analyticsListener?.onApiError(error.apiName, error.code, error.message)
    }

    private fun replaceLoadingWithResponse(
        loadingMsgId: String,
        text: String,
        followUpQuestions: List<String>?,
        messageId: String?
    ) {
        val aiMsg = ChatMessage.AiResponse(
            text = text,
            followUpQuestions = followUpQuestions,
            messageId = messageId
        )
        _state.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == loadingMsgId) aiMsg else msg
                }
            )
        }
    }

    private fun removeLoadingAndSetError(loadingMsgId: String, errorMsg: String) {
        _state.update { state ->
            state.copy(
                messages = state.messages.filter { it.id != loadingMsgId },
                errorMessage = errorMsg,
                failedMessageId = loadingMsgId,
                chatResponseState = UiState.Error(errorMsg)
            )
        }
    }

    private fun appendMessages(vararg messages: ChatMessage) {
        _state.update { state ->
            state.copy(messages = state.messages + messages.toList())
        }
    }

    /**
     * API returns sections newest-first. Each section contains [user_query, ai_response, follow_ups].
     * This reverses the section order while keeping items within each section in their original order.
     */
    private fun reverseHistorySections(
        items: List<ConversationChatHistoryMessageItem>
    ): List<ConversationChatHistoryMessageItem> {
        val sections = mutableListOf<List<ConversationChatHistoryMessageItem>>()
        var current = mutableListOf<ConversationChatHistoryMessageItem>()
        for (item in items) {
            // User message types start a new section: 1=text, 2=audio, 11=image
            val startsSection = item.message_type_id in listOf(1, 2, 11)
            if (startsSection && current.isNotEmpty()) {
                sections.add(current.toList())
                current = mutableListOf()
            }
            current.add(item)
        }
        if (current.isNotEmpty()) sections.add(current.toList())
        sections.reverse()
        return sections.flatten()
    }

    private fun convertHistoryToMessages(
        items: List<ConversationChatHistoryMessageItem>
    ): List<ChatMessage> {
        // message_type_id values from API:
        //  1 = user text query
        //  2 = user audio query (heard_query_text)
        //  3 = AI text response
        //  7 = follow-up questions block (skip — they're nested in the AI response item)
        // 11 = user image query (query_media_file_url)
        val result = mutableListOf<ChatMessage>()
        for (item in items) {
            when (item.message_type_id) {
                1 -> { // user text query
                    val text = item.query_text ?: ""
                    if (text.isNotEmpty()) result.add(ChatMessage.UserMessage(text = text))
                }
                2 -> { // user audio query
                    val text = item.heard_query_text ?: item.query_text ?: ""
                    if (text.isNotEmpty()) result.add(ChatMessage.UserMessage(text = text))
                }
                3 -> { // AI response — questions arrive via a separate type_7 item
                    val text = item.response_text ?: ""
                    if (text.isNotEmpty()) {
                        result.add(ChatMessage.AiResponse(
                            text = text,
                            followUpQuestions = null,
                            followUpQuestionIds = null,
                            messageId = item.message_id
                        ))
                    }
                }
                7 -> { // follow-up questions block — attach to the preceding AI response
                    val qs = item.questions?.takeIf { it.isNotEmpty() } ?: continue
                    val lastAiIdx = result.indexOfLast { it is ChatMessage.AiResponse }
                    if (lastAiIdx >= 0) {
                        val lastAi = result[lastAiIdx] as ChatMessage.AiResponse
                        result[lastAiIdx] = lastAi.copy(
                            followUpQuestions = qs.map { it.question },
                            followUpQuestionIds = qs.map { it.follow_up_question_id }
                        )
                    }
                }
                11 -> { // user image query
                    val text = item.query_text ?: ""
                    result.add(ChatMessage.UserMessage(text = text, imageUri = null))
                }
                else -> {
                    // Fallback: render query + response if both present in the same item
                    val text = item.query_text ?: item.heard_query_text
                    text?.let { if (it.isNotEmpty()) result.add(ChatMessage.UserMessage(text = it)) }
                    item.response_text?.let { responseText ->
                        if (responseText.isNotEmpty()) {
                            val followUps = item.questions?.map { it.question }
                            val followUpIds = item.questions?.map { it.follow_up_question_id }
                            result.add(ChatMessage.AiResponse(
                                text = responseText,
                                followUpQuestions = followUps,
                                followUpQuestionIds = followUpIds,
                                messageId = item.message_id
                            ))
                        }
                    }
                }
            }
        }
        return result
    }
}
