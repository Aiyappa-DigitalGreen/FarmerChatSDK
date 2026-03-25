package com.farmerchat.sdk.domain.model.language

import com.google.gson.annotations.SerializedName

data class SupportedLanguage(
    val id: Int,
    val name: String,
    val code: String,
    @SerializedName("display_name")
    val displayName: String = "",
    val flag: String? = null,
    @SerializedName("asr_enabled")
    val isAsrEnabled: Boolean = false,
    @SerializedName("tts_enabled")
    val isTtsEnabled: Boolean = false
)

data class SupportedLanguageGroup(
    val displayName: String,
    val flag: String,
    val languages: List<SupportedLanguage>
)

data class SetPreferredLanguageRequest(
    val user_id: String,
    val language_id: String
)

data class SetPreferredLanguageResponse(
    val user_id: String
)
