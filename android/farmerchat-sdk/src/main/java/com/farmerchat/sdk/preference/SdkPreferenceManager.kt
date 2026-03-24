package com.farmerchat.sdk.preference

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

private const val TAG = "SdkPreferenceManager"

internal class SdkPreferenceManager(context: Context) {

    private val prefs: SharedPreferences = createPrefs(context)

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

        private fun createPrefs(context: Context): SharedPreferences {
            return try {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (e: Exception) {
                // Fallback to plain prefs if encryption setup fails (e.g. device without secure hardware)
                Log.w(TAG, "EncryptedSharedPreferences unavailable, falling back to plain prefs: ${e.message}")
                context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            }
        }
    }
}
