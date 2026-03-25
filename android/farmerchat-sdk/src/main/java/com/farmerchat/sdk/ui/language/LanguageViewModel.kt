package com.farmerchat.sdk.ui.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.farmerchat.sdk.base.ApiResult
import com.farmerchat.sdk.base.UiState
import com.farmerchat.sdk.domain.model.language.SetPreferredLanguageRequest
import com.farmerchat.sdk.domain.model.language.SupportedLanguage
import com.farmerchat.sdk.domain.model.language.SupportedLanguageGroup
import com.farmerchat.sdk.domain.usecase.LanguageUseCase
import com.farmerchat.sdk.preference.SdkPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class LanguageScreenState(
    val languageState: UiState<List<SupportedLanguageGroup>> = UiState.Idle,
    val selectedLanguageId: Int? = null,
    val selectedLanguageName: String = "",
    val submitState: UiState<Unit> = UiState.Idle
)

internal class LanguageViewModel(
    private val languageUseCase: LanguageUseCase,
    private val preferenceManager: SdkPreferenceManager,
    private val countryCode: String = "IN",
    private val stateCode: String = ""
) : ViewModel() {

    private val _state = MutableStateFlow(LanguageScreenState())
    val state: StateFlow<LanguageScreenState> = _state

    init {
        fetchLanguages()
    }

    fun fetchLanguages() {
        viewModelScope.launch {
            _state.update { it.copy(languageState = UiState.Loading) }
            languageUseCase.getSupportedLanguages(countryCode = countryCode, state = stateCode).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val groups = result.data
                        // Auto-select English if available
                        val english = groups.flatMap { it.languages }
                            .firstOrNull { it.code.equals("en", ignoreCase = true) }
                        _state.update {
                            it.copy(
                                languageState = UiState.Success(groups),
                                selectedLanguageId = english?.id,
                                selectedLanguageName = english?.displayName?.ifBlank { english.name } ?: ""
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        _state.update {
                            it.copy(languageState = UiState.Error(result.message ?: "Failed to load languages"))
                        }
                    }
                }
            }
        }
    }

    fun selectLanguage(language: SupportedLanguage) {
        _state.update {
            it.copy(
                selectedLanguageId = language.id,
                selectedLanguageName = language.displayName.ifBlank { language.name }
            )
        }
    }

    fun submitLanguage(onSuccess: (String) -> Unit) {
        val langId = _state.value.selectedLanguageId ?: return
        val langName = _state.value.selectedLanguageName
        val userId = preferenceManager.getUserId() ?: return

        viewModelScope.launch {
            _state.update { it.copy(submitState = UiState.Loading) }
            languageUseCase.setPreferredLanguage(
                SetPreferredLanguageRequest(
                    user_id = userId,
                    language_id = langId.toString()
                )
            ).collect { result ->
                when (result) {
                    is ApiResult.Success -> {
                        preferenceManager.saveSelectedLanguage(langName)
                        _state.update { it.copy(submitState = UiState.Success(Unit)) }
                        onSuccess(langName)
                    }
                    is ApiResult.Error -> {
                        // Even on API error, save locally and proceed
                        preferenceManager.saveSelectedLanguage(langName)
                        _state.update { it.copy(submitState = UiState.Idle) }
                        onSuccess(langName)
                    }
                }
            }
        }
    }
}
