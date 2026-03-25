package com.farmerchat.sdk.di

import com.farmerchat.sdk.ui.chat.ChatViewModel
import com.farmerchat.sdk.ui.history.HistoryViewModel
import com.farmerchat.sdk.ui.language.LanguageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val sdkViewModelModule = module {

    viewModel { (conversationId: String?) ->
        ChatViewModel(
            chatUseCase = get(),
            conversationRepository = get(),
            preferenceManager = get(),
            context = androidContext(),
            initialConversationId = conversationId
        )
    }

    viewModel {
        HistoryViewModel(
            historyUseCase = get(),
            preferenceManager = get()
        )
    }

    viewModel {
        val config = runCatching { com.farmerchat.sdk.FarmerChatSdk.config }.getOrNull()
        // Auto-detect country from SIM/network/locale; config override wins if explicitly set
        val detectedCountry = com.farmerchat.sdk.utils.CountryDetector.detect(
            context = androidContext(),
            fallback = config?.countryCode ?: "IN"
        )
        val effectiveCountry = if (config?.countryCode != null &&
            config.countryCode != "IN") config.countryCode else detectedCountry
        LanguageViewModel(
            languageUseCase = get(),
            preferenceManager = get(),
            countryCode = effectiveCountry,
            stateCode = config?.stateCode ?: ""
        )
    }
}
