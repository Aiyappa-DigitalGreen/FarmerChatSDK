package com.farmerchat.sdk.di

import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.auth.TokenStore
import com.farmerchat.sdk.data.remote.ChatApiService
import com.farmerchat.sdk.data.remote.SdkAuthApiService
import com.farmerchat.sdk.network.SdkAuthHeaderInterceptor
import com.farmerchat.sdk.network.SdkTokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

private const val QUALIFIER_AUTH = "auth"
private const val QUALIFIER_MAIN = "main"

internal val sdkNetworkModule = module {

    single<HttpLoggingInterceptor> {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single<SdkAuthHeaderInterceptor> {
        SdkAuthHeaderInterceptor(
            context = androidContext(),
            tokenStore = get()
        )
    }

    single<SdkTokenAuthenticator> {
        SdkTokenAuthenticator(
            tokenStore = get(),
            authApiProvider = { get(named(QUALIFIER_AUTH)) }
        )
    }

    // Auth OkHttpClient — no authenticator to prevent infinite loops
    single<OkHttpClient>(named(QUALIFIER_AUTH)) {
        OkHttpClient.Builder()
            .addInterceptor(get<SdkAuthHeaderInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Main OkHttpClient — with token authenticator
    single<OkHttpClient>(named(QUALIFIER_MAIN)) {
        OkHttpClient.Builder()
            .addInterceptor(get<SdkAuthHeaderInterceptor>())
            .addInterceptor(get<HttpLoggingInterceptor>())
            .authenticator(get<SdkTokenAuthenticator>())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Auth Retrofit
    single<Retrofit>(named(QUALIFIER_AUTH)) {
        Retrofit.Builder()
            .baseUrl(FarmerChatSdk.config.baseUrl)
            .client(get(named(QUALIFIER_AUTH)))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Main Retrofit
    single<Retrofit>(named(QUALIFIER_MAIN)) {
        Retrofit.Builder()
            .baseUrl(FarmerChatSdk.config.baseUrl)
            .client(get(named(QUALIFIER_MAIN)))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single<SdkAuthApiService>(named(QUALIFIER_AUTH)) {
        get<Retrofit>(named(QUALIFIER_AUTH)).create(SdkAuthApiService::class.java)
    }

    single<ChatApiService> {
        get<Retrofit>(named(QUALIFIER_MAIN)).create(ChatApiService::class.java)
    }
}
