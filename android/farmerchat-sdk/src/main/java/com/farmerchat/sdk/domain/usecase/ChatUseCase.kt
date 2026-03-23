package com.farmerchat.sdk.domain.usecase

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.data.repository.ChatRepository
import com.farmerchat.sdk.data.repository.ConversationRepository
import com.farmerchat.sdk.domain.model.chat.*
import com.farmerchat.sdk.domain.model.conversation.NewConversationRequest
import com.farmerchat.sdk.domain.model.conversation.NewConversationResponse
import com.farmerchat.sdk.domain.model.voice.GetVoiceResponse
import com.farmerchat.sdk.domain.model.voice.SetVoiceRequest
import kotlinx.coroutines.flow.Flow

internal class ChatUseCase(
    private val chatRepository: ChatRepository,
    private val conversationRepository: ConversationRepository
) {

    suspend fun newConversation(userId: String, contentProviderId: String?): ApiResult<NewConversationResponse> {
        return conversationRepository.newConversation(
            NewConversationRequest(user_id = userId, content_provider_id = contentProviderId)
        )
    }

    fun sendTextQuery(
        query: String,
        conversationId: String,
        messageId: String,
        triggeredInputType: String = "text",
        transcriptionId: String? = null,
        statementId: String? = null,
        retry: Boolean = false
    ): Flow<ApiResult<TextPromptResponse>> {
        return chatRepository.getTextPrompt(
            TextPromptRequest(
                query = query,
                conversation_id = conversationId,
                message_id = messageId,
                triggered_input_type = triggeredInputType,
                transcription_id = transcriptionId,
                statement_id = statementId,
                retry = retry
            )
        )
    }

    fun sendImageQuery(
        conversationId: String,
        imageBase64: String,
        imageName: String,
        query: String? = null,
        latitude: String? = null,
        longitude: String? = null,
        retry: Boolean = false
    ): Flow<ApiResult<PlantixResponse>> {
        return chatRepository.getPlantixAnalysis(
            PlantixRequest(
                conversation_id = conversationId,
                image = imageBase64,
                image_name = imageName,
                query = query,
                latitude = latitude,
                longitude = longitude,
                retry = retry
            )
        )
    }

    fun getFollowUpQuestions(messageId: String): Flow<ApiResult<FollowUpQuestionsResponse>> {
        return chatRepository.getFollowUpQuestions(messageId)
    }

    fun trackFollowUpQuestionClick(question: String): Flow<ApiResult<FollowUpQuestionClickResponse>> {
        return chatRepository.postFollowUpQuestionClick(
            FollowUpQuestionClickRequest(follow_up_question = question)
        )
    }

    fun synthesiseAudio(messageId: String, text: String, userId: String): Flow<ApiResult<SynthesiseAudioResponse>> {
        return chatRepository.synthesiseAudio(
            SynthesiseAudioRequest(
                message_id = messageId,
                text = text,
                user_id = userId
            )
        )
    }

    fun getChatHistory(conversationId: String, page: Int): Flow<ApiResult<ConversationChatHistoryResponse>> {
        return chatRepository.getChatHistory(conversationId, page)
    }

    fun transcribeAudio(
        conversationId: String,
        audioBase64: String,
        messageReferenceId: String,
        audioFormat: String
    ): Flow<ApiResult<GetVoiceResponse>> {
        return chatRepository.transcribeAudio(
            SetVoiceRequest(
                conversation_id = conversationId,
                query = audioBase64,
                message_reference_id = messageReferenceId,
                input_audio_encoding_format = audioFormat,
                triggered_input_type = "audio"
            )
        )
    }
}
