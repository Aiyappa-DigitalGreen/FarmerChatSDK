package com.farmerchat.sdk.data.remote

import com.farmerchat.sdk.domain.model.chat.*
import com.farmerchat.sdk.domain.model.conversation.NewConversationRequest
import com.farmerchat.sdk.domain.model.conversation.NewConversationResponse
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageRequest
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageResponse
import com.farmerchat.sdk.domain.model.language.SupportedLanguageGroup
import com.farmerchat.sdk.domain.model.voice.GetVoiceResponse
import com.farmerchat.sdk.domain.model.voice.SetVoiceRequest
import retrofit2.Response
import retrofit2.http.*

internal interface ChatApiService {

    @GET("api/language/v2/country_wise_supported_languages/")
    suspend fun getSupportedLanguages(
        @Query("country_code") countryCode: String = "",
        @Query("state") state: String = ""
    ): Response<List<SupportedLanguageGroup>>

    @POST("api/user/set_preferred_language/")
    suspend fun setPreferredLanguage(
        @Body request: SetPreferredLanguageRequest
    ): Response<SetPreferredLanguageResponse>

    @POST("api/chat/new_conversation/")
    suspend fun newConversation(
        @Body request: NewConversationRequest
    ): Response<NewConversationResponse>

    @POST("api/chat/get_answer_for_text_query/")
    suspend fun getTextPrompt(
        @Body request: TextPromptRequest
    ): Response<TextPromptResponse>

    @POST("api/chat/image_analysis/")
    suspend fun getPlantixAnalysis(
        @Body request: PlantixRequest
    ): Response<PlantixResponse>

    @GET("api/chat/follow_up_questions/")
    suspend fun getFollowUpQuestions(
        @Query("message_id") messageId: String,
        @Query("use_latest_prompt") useLatestPrompt: Boolean = true
    ): Response<FollowUpQuestionsResponse>

    @POST("api/chat/follow_up_question_click/")
    suspend fun postTrackFollowUpQuestion(
        @Body request: FollowUpQuestionClickRequest
    ): Response<FollowUpQuestionClickResponse>

    @POST("api/chat/synthesise_audio/")
    suspend fun synthesiseAudio(
        @Body request: SynthesiseAudioRequest
    ): Response<SynthesiseAudioResponse>

    @GET("api/chat/conversation_chat_history/")
    suspend fun getChatHistory(
        @Query("conversation_id") conversationId: String,
        @Query("page") page: Int = 1
    ): Response<ConversationChatHistoryResponse>

    @GET("api/chat/conversation_list/")
    suspend fun getConversationList(
        @Query("page") page: Int = 1,
        @Query("user_id") userId: String
    ): Response<List<com.farmerchat.sdk.domain.model.history.ConversationListItem>>

    @POST("api/chat/transcribe_audio/")
    suspend fun transcribeAudio(
        @Body request: SetVoiceRequest
    ): Response<GetVoiceResponse>
}
