package com.farmerchat.sdk.preference

import android.content.Context
import android.content.SharedPreferences

internal class SdkPreferenceManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )

    fun saveTokens(
        accessToken: String? = null,
        refreshToken: String? = null,
        userId: String? = null,
        deviceId: String? = null
    ) {
        prefs.edit().apply {
            accessToken?.let { putString(KEY_ACCESS_TOKEN, it) }
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            userId?.let { putString(KEY_USER_ID, it) }
            deviceId?.let { putString(KEY_DEVICE_ID, it) }
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    fun getDeviceId(): String? = prefs.getString(KEY_DEVICE_ID, null)

    fun hasTokens(): Boolean =
        prefs.getString(KEY_ACCESS_TOKEN, null) != null &&
                prefs.getString(KEY_REFRESH_TOKEN, null) != null &&
                prefs.getString(KEY_USER_ID, null) != null

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "farmerchat_sdk_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_DEVICE_ID = "device_id"
    }
}
