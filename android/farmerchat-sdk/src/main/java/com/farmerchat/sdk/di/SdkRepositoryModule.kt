package com.farmerchat.sdk.di

import com.farmerchat.sdk.data.repository.ChatRepository
import com.farmerchat.sdk.data.repository.ConversationRepository
import com.farmerchat.sdk.data.repository.HistoryRepository
import com.farmerchat.sdk.data.repository.LanguageRepository
import org.koin.dsl.module

internal val sdkRepositoryModule = module {

    single<ChatRepository> {
        ChatRepository(get())
    }

    single<HistoryRepository> {
        HistoryRepository(get())
    }

    single<ConversationRepository> {
        ConversationRepository(get())
    }

    single<LanguageRepository> {
        LanguageRepository(get())
    }
}
