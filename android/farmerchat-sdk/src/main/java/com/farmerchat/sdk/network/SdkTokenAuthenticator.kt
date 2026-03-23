package com.farmerchat.sdk.network

import android.util.Log
import com.farmerchat.sdk.auth.TokenStore
import com.farmerchat.sdk.data.remote.SdkAuthApiService
import com.farmerchat.sdk.domain.model.token.RefreshTokenRequest
import com.farmerchat.sdk.domain.model.token.SendNewTokenRequest
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

private const val TAG = "SdkTokenAuthenticator"

// Endpoints that should NOT trigger auth refresh
private val SKIP_AUTH_PATHS = setOf(
    "api/user/get_new_access_token/",
    "api/user/send_tokens/",
    "api/user/initialize_user/"
)

private const val GUEST_USER_API_KEY = "Y2K3kW5R9uQ0fL2X8zI7hT3aJ7"
private const val MAX_RETRY_COUNT = 1

internal class SdkTokenAuthenticator(
    private val tokenStore: TokenStore,
    private val authApiProvider: () -> SdkAuthApiService
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Don't retry more than once
        if (responseCount(response) > MAX_RETRY_COUNT) return null

        // Skip auth refresh for auth endpoints
        val path = response.request.url.encodedPath
        if (SKIP_AUTH_PATHS.any { path.contains(it) }) return null

        val authApi = authApiProvider()

        synchronized(this) {
            // Check if another thread already refreshed the token
            val currentToken = tokenStore.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")
            if (!currentToken.isNullOrEmpty() && currentToken != requestToken) {
                // Token was already refreshed by another thread
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Step 1: Try refresh token
            val refreshToken = tokenStore.getRefreshToken()
            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val refreshRes = authApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()
                    if (refreshRes.isSuccessful) {
                        val body = refreshRes.body()
                        if (!body?.access_token.isNullOrEmpty()) {
                            tokenStore.saveTokens(
                                accessToken = body!!.access_token,
                                refreshToken = body.refresh_token ?: refreshToken
                            )
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer ${body.access_token}")
                                .build()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Token refresh failed", e)
                }
            }

            // Step 2: Fallback — send user tokens to get new ones
            val deviceId = tokenStore.getDeviceId() ?: return null
            val userId = tokenStore.getUserId() ?: return null
            return try {
                val sendTokenRes = authApi.sendUserTokens(
                    GUEST_USER_API_KEY,
                    SendNewTokenRequest(device_id = deviceId, user_id = userId)
                ).execute()

                if (sendTokenRes.isSuccessful) {
                    val body = sendTokenRes.body()
                    if (!body?.access_token.isNullOrEmpty()) {
                        tokenStore.saveTokens(
                            accessToken = body!!.access_token,
                            refreshToken = body.refresh_token
                        )
                        response.request.newBuilder()
                            .header("Authorization", "Bearer ${body.access_token}")
                            .build()
                    } else null
                } else null
            } catch (e: Exception) {
                Log.e(TAG, "Send tokens fallback failed", e)
                null
            }
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
