package com.farmerchat.sdk.domain.usecase

import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.BaseUseCase
import com.farmerchat.sdk.data.repository.LanguageRepository
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageRequest
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageResponse
import com.farmerchat.sdk.domain.model.language.SupportedLanguageGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class LanguageUseCase(
    private val repo: LanguageRepository
) : BaseUseCase() {

    fun getSupportedLanguages(
        countryCode: String = "",
        state: String = ""
    ): Flow<ApiResult<List<SupportedLanguageGroup>>> = flow {
        emit(
            executeApiCall("get_supported_languages") {
                repo.fetchSupportedLanguages(countryCode, state)
            }
        )
    }

    fun setPreferredLanguage(
        request: SetPreferredLanguageRequest
    ): Flow<ApiResult<SetPreferredLanguageResponse>> = flow {
        emit(
            executeApiCall("set_preferred_language") {
                repo.setPreferredLanguage(request)
            }
        )
    }
}
