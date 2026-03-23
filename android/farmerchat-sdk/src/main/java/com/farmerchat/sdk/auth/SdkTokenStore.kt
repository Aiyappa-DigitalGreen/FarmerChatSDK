package com.farmerchat.sdk.auth

import com.farmerchat.sdk.preference.SdkPreferenceManager

/**
 * Public interface that SDK consumers can implement to provide custom token storage.
 */
interface TokenStore {
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun saveTokens(accessToken: String?, refreshToken: String?)
    fun getUserId(): String?
    fun getDeviceId(): String?
    fun clear()
}

/**
 * Default internal implementation backed by SharedPreferences.
 */
internal class SdkTokenStore(
    private val preferenceManager: SdkPreferenceManager
) : TokenStore {

    override fun getAccessToken(): String? = preferenceManager.getAccessToken()

    override fun getRefreshToken(): String? = preferenceManager.getRefreshToken()

    override fun saveTokens(accessToken: String?, refreshToken: String?) {
        preferenceManager.saveTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    override fun getUserId(): String? = preferenceManager.getUserId()

    override fun getDeviceId(): String? = preferenceManager.getDeviceId()

    override fun clear() = preferenceManager.clear()
}
