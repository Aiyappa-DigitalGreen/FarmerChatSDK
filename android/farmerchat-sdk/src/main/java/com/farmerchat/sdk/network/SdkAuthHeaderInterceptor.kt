package com.farmerchat.sdk.network

import android.content.Context
import com.farmerchat.sdk.auth.TokenStore
import com.farmerchat.sdk.utils.getEncodedDeviceConfig
import okhttp3.Interceptor
import okhttp3.Response

internal class SdkAuthHeaderInterceptor(
    private val context: Context,
    private val tokenStore: TokenStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // SDK API key — identifies this integration to the FarmerChat backend
        val sdkApiKey = try { com.farmerchat.sdk.FarmerChatSdk.config.sdkApiKey } catch (_: Exception) { null }
        if (!sdkApiKey.isNullOrEmpty()) {
            requestBuilder.addHeader("X-SDK-Key", sdkApiKey)
        }

        requestBuilder.addHeader("Build-Version", "v2")
        requestBuilder.addHeader("Device-Info", context.getEncodedDeviceConfig())

        val token = tokenStore.getAccessToken()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
