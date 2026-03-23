package com.farmerchat.sdk.di

import com.farmerchat.sdk.domain.usecase.ChatUseCase
import com.farmerchat.sdk.domain.usecase.HistoryUseCase
import org.koin.dsl.module

internal val sdkUseCaseModule = module {

    factory<ChatUseCase> {
        ChatUseCase(get(), get())
    }

    factory<HistoryUseCase> {
        HistoryUseCase(get())
    }
}
