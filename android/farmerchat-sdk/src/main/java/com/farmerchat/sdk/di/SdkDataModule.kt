package com.farmerchat.sdk.di

import com.farmerchat.sdk.auth.SdkTokenStore
import com.farmerchat.sdk.auth.TokenStore
import com.farmerchat.sdk.preference.SdkPreferenceManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val sdkDataModule = module {

    single<SdkPreferenceManager> {
        SdkPreferenceManager(androidContext())
    }

    single<TokenStore> {
        SdkTokenStore(get())
    }
}
