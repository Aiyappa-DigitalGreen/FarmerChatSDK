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
        LanguageViewModel(
            languageUseCase = get(),
            preferenceManager = get()
        )
    }
}
