package com.farmerchat.sdk.network

import android.util.Log
import com.farmerchat.sdk.domain.model.guest.InitializeGuestUserRequest
import com.farmerchat.sdk.domain.model.guest.InitializeGuestUserResponse
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "GuestApiClient"

internal object GuestApiClient {

    private const val GUEST_API_KEY = "Y2K3kW5R9uQ0fL2X8zI7hT3aJ7"
    private const val ENDPOINT = "api/user/initialize_user/"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class)
    fun initializeUser(baseUrl: String, deviceId: String): InitializeGuestUserResponse {
        val url = baseUrl.trimEnd('/') + "/" + ENDPOINT
        Log.d(TAG, "Calling initialize_user: $url | device_id=$deviceId")

        val body = gson.toJson(InitializeGuestUserRequest(device_id = deviceId))
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("API-Key", GUEST_API_KEY)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            Log.d(TAG, "initialize_user HTTP ${response.code}: $responseBody")

            if (!response.isSuccessful) {
                throw IOException("initialize_user failed: HTTP ${response.code} — $responseBody")
            }

            val parsed = gson.fromJson(responseBody, InitializeGuestUserResponse::class.java)
            Log.d(TAG, "Parsed: access_token=${parsed.access_token.take(10)}... user_id=${parsed.user_id} refresh=${parsed.refresh_token.take(10)}...")

            // Fallback: if user_id is null, try to extract it from the raw JSON
            // (handles case where API returns user_id as a number, not a string)
            if (parsed.user_id == null) {
                val rawJson = com.google.gson.JsonParser.parseString(responseBody).asJsonObject
                val rawUserId = rawJson.get("user_id")
                Log.w(TAG, "user_id was null in parsed model, raw JSON value: $rawUserId")
                if (rawUserId != null && !rawUserId.isJsonNull) {
                    return parsed.copy(user_id = rawUserId.asString)
                }
            }
            return parsed
        }
    }
}
