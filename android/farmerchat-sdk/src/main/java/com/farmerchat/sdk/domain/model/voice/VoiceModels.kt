package com.farmerchat.sdk.domain.model.voice

import com.google.gson.annotations.SerializedName

data class SetVoiceRequest(
    @SerializedName("conversation_id") val conversation_id: String,
    @SerializedName("query") val query: String,
    @SerializedName("message_reference_id") val message_reference_id: String,
    @SerializedName("input_audio_encoding_format") val input_audio_encoding_format: String,
    @SerializedName("triggered_input_type") val triggered_input_type: String,
    @SerializedName("editable_transcription") val editable_transcription: String = "True"
)

data class GetVoiceResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("heard_input_query") val heard_input_query: String?,
    @SerializedName("confidence_score") val confidence_score: Double?,
    @SerializedName("error") val error: Boolean,
    @SerializedName("message_id") val message_id: String,
    @SerializedName("transcription_id") val transcription_id: String?
)
