package com.farmerchat.sdk.di

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

internal object SdkKoinHolder {

    private var koinApp: KoinApplication? = null

    val koin: Koin
        get() = koinApp?.koin ?: error("FarmerChat SDK Koin not started. Call FarmerChatSdk.initialize() first.")

    fun start(context: Context) {
        if (koinApp != null) return
        koinApp = koinApplication {
            androidContext(context.applicationContext)
            modules(
                sdkDataModule,
                sdkNetworkModule,
                sdkRepositoryModule,
                sdkUseCaseModule,
                sdkViewModelModule
            )
        }
    }

    fun stop() {
        koinApp?.close()
        koinApp = null
    }
}
