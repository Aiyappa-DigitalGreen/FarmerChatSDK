package com.farmerchat.sdk.data.repository

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.BaseUseCase
import com.farmerchat.sdk.data.remote.ChatApiService
import com.farmerchat.sdk.domain.model.chat.*
import com.farmerchat.sdk.domain.model.voice.GetVoiceResponse
import com.farmerchat.sdk.domain.model.voice.SetVoiceRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class ChatRepository(
    private val apiService: ChatApiService
) : BaseUseCase() {

    fun getTextPrompt(request: TextPromptRequest): Flow<ApiResult<TextPromptResponse>> = flow {
        emit(executeApiCall("getTextPrompt") { apiService.getTextPrompt(request) })
    }

    fun getPlantixAnalysis(request: PlantixRequest): Flow<ApiResult<PlantixResponse>> = flow {
        emit(executeApiCall("getPlantixAnalysis") { apiService.getPlantixAnalysis(request) })
    }

    fun getFollowUpQuestions(messageId: String): Flow<ApiResult<FollowUpQuestionsResponse>> = flow {
        emit(executeApiCall("getFollowUpQuestions") { apiService.getFollowUpQuestions(messageId) })
    }

    fun postFollowUpQuestionClick(request: FollowUpQuestionClickRequest): Flow<ApiResult<FollowUpQuestionClickResponse>> = flow {
        emit(executeApiCall("postFollowUpQuestionClick") { apiService.postTrackFollowUpQuestion(request) })
    }

    fun synthesiseAudio(request: SynthesiseAudioRequest): Flow<ApiResult<SynthesiseAudioResponse>> = flow {
        emit(executeApiCall("synthesiseAudio") { apiService.synthesiseAudio(request) })
    }

    fun getChatHistory(conversationId: String, page: Int): Flow<ApiResult<ConversationChatHistoryResponse>> = flow {
        emit(executeApiCall("getChatHistory") { apiService.getChatHistory(conversationId, page) })
    }

    fun transcribeAudio(request: SetVoiceRequest): Flow<ApiResult<GetVoiceResponse>> = flow {
        emit(executeApiCall("transcribeAudio") { apiService.transcribeAudio(request) })
    }
}
