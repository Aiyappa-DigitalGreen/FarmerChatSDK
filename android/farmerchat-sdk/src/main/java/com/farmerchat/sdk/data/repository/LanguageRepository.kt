package com.farmerchat.sdk.data.repository

import com.farmerchat.sdk.data.remote.ChatApiService
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageRequest
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageResponse
import com.farmerchat.sdk.domain.model.language.SupportedLanguageGroup
import retrofit2.Response

internal class LanguageRepository(private val api: ChatApiService) {

    suspend fun fetchSupportedLanguages(
        countryCode: String = "",
        state: String = ""
    ): Response<List<SupportedLanguageGroup>> =
        api.getSupportedLanguages(countryCode = countryCode, state = state)

    suspend fun setPreferredLanguage(
        request: SetPreferredLanguageRequest
    ): Response<SetPreferredLanguageResponse> =
        api.setPreferredLanguage(request)
}
