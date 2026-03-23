package com.farmerchat.sdk.domain.model.chat

import com.google.gson.annotations.SerializedName

data class TextPromptRequest(
    @SerializedName("query") val query: String,
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("statement_id") val statement_id: String? = null,
    @SerializedName("weather_cta_triggered") val weather_cta_triggered: Boolean = false,
    @SerializedName("triggered_input_type") val triggered_input_type: String,
    @SerializedName("ssfr_crop") val ssfr_crop: String? = null,
    @SerializedName("use_entity_extraction") val use_entity_extraction: Boolean = true,
    @SerializedName("transcription_id") val transcription_id: String? = null,
    @SerializedName("retry") val retry: Boolean = false
)

data class TextPromptResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("message_id") val message_id: String?,
    @SerializedName("response") val response: String?,
    @SerializedName("translated_response") val translated_response: String?,
    @SerializedName("follow_up_questions") val follow_up_questions: List<FollowUpQuestionOption>?,
    @SerializedName("section_message_id") val section_message_id: String?,
    @SerializedName("content_provider_logo") val content_provider_logo: String?,
    @SerializedName("hide_follow_up_question") val hide_follow_up_question: Boolean? = null,
    @SerializedName("hide_tts_speaker") val hide_tts_speaker: Boolean? = null,
    @SerializedName("points") val points: Int?,
    @SerializedName("intent_classification_output") val intent_classification_output: IntentClassificationOutput? = null
)

data class IntentClassificationOutput(
    @SerializedName("intent") val intent: String?,
    @SerializedName("confidence") val confidence: String?,   // API returns "high"/"medium"/"low"
    @SerializedName("asset_type") val asset_type: String?,
    @SerializedName("asset_name") val asset_name: String?,
    @SerializedName("asset_status") val asset_status: String?,
    @SerializedName("concern") val concern: String?,
    @SerializedName("stage") val stage: String?,
    @SerializedName("likely_activity") val likely_activity: String?,
    @SerializedName("rephrased_query") val rephrased_query: String?,
    @SerializedName("seasonal_relevance") val seasonal_relevance: String?
)

data class PlantixRequest(
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("image") val image: String,
    @SerializedName("triggered_input_type") val triggered_input_type: String = "image",
    @SerializedName("query") val query: String? = null,
    @SerializedName("latitude") val latitude: String? = null,
    @SerializedName("longitude") val longitude: String? = null,
    @SerializedName("image_name") val image_name: String,
    @SerializedName("retry") val retry: Boolean = false
)

data class PlantixResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("response") val response: String,
    @SerializedName("follow_up_questions") val follow_up_questions: List<FollowUpQuestionOption>?,
    @SerializedName("content_provider_logo") val content_provider_logo: String?,
    @SerializedName("hide_tts_speaker") val hide_tts_speaker: Boolean?,
    @SerializedName("points") val points: Int?
)

data class FollowUpQuestionsResponse(
    @SerializedName("message_id") val message_id: String,
    @SerializedName("questions") val questions: List<Question>?,
    @SerializedName("section_message_id") val section_message_id: String
)

data class Question(
    @SerializedName("follow_up_question_id") val follow_up_question_id: String,
    @SerializedName("question") val question: String,
    @SerializedName("sequence") val sequence: Int
)

data class FollowUpQuestionClickRequest(
    @SerializedName("follow_up_question") val follow_up_question: String
)

data class FollowUpQuestionClickResponse(
    @SerializedName("message") val message: String? = null
)

data class SynthesiseAudioRequest(
    @SerializedName("message_id") val message_id: String,
    @SerializedName("text") val text: String,
    @SerializedName("user_id") val user_id: String
)

data class SynthesiseAudioResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("error") val error: Boolean = false,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("text") val text: String? = null
)

data class ConversationChatHistoryResponse(
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("data") val data: List<ConversationChatHistoryMessageItem>
)

data class ConversationChatHistoryMessageItem(
    @SerializedName("message_type_id") val message_type_id: Int,
    @SerializedName("message_type") val message_type: String,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("query_text") val query_text: String? = null,
    @SerializedName("heard_query_text") val heard_query_text: String? = null,
    @SerializedName("response_text") val response_text: String? = null,
    @SerializedName("questions") val questions: List<ConversationChatHistoryQuestion>? = null,
    @SerializedName("query_media_file_url") val query_media_file_url: String? = null,
    @SerializedName("content_provider_logo") val content_provider_logo: String? = null,
    @SerializedName("hide_tts_speaker") val hide_tts_speaker: Boolean? = null
)

data class ConversationChatHistoryQuestion(
    @SerializedName("follow_up_question_id") val follow_up_question_id: String,
    @SerializedName("question") val question: String,
    @SerializedName("sequence") val sequence: Int
)

data class FollowUpQuestionOption(
    @SerializedName("follow_up_question_id") val follow_up_question_id: String?,
    @SerializedName("sequence") val sequence: Int,
    @SerializedName("question") val question: String?
)
