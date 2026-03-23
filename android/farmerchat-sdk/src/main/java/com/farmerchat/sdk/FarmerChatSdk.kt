package com.farmerchat.sdk

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.farmerchat.sdk.activity.FarmerChatActivity
import com.farmerchat.sdk.base.AuthInitException
import com.farmerchat.sdk.di.SdkKoinHolder
import com.farmerchat.sdk.network.GuestApiClient
import com.farmerchat.sdk.preference.SdkPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private const val TAG = "FarmerChatSdk"

object FarmerChatSdk {

    private var _config: FarmerChatConfig? = null
    private var appContext: Context? = null
    private val sdkScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val initMutex = Mutex()

    internal val config: FarmerChatConfig
        get() = _config ?: error("FarmerChatSdk not initialized.")

    private var initialized = false

    fun initialize(context: Context, config: FarmerChatConfig) {
        require(isValidSdkApiKey(config.sdkApiKey)) {
            "Invalid SDK API key '${config.sdkApiKey}'. " +
            "Keys must start with 'fc_live_' or 'fc_test_' followed by at least 16 alphanumeric characters. " +
            "Obtain your key from the FarmerChat developer portal."
        }
        _config = config
        appContext = context.applicationContext
        val deviceId = Settings.Secure.getString(appContext!!.contentResolver, Settings.Secure.ANDROID_ID)
        val prefs = SdkPreferenceManager(appContext!!)

        prefs.saveTokens(deviceId = deviceId)
        SdkKoinHolder.start(appContext!!)
        initialized = true

        Log.d(TAG, "initialize() — deviceId=$deviceId hasTokens=${prefs.hasTokens()}")

        if (!prefs.hasTokens()) {
            sdkScope.launch {
                try { fetchGuestTokens(prefs, deviceId, config.baseUrl) }
                catch (e: Exception) { Log.w(TAG, "Background init failed (will retry on openChat): ${e.message}") }
            }
        }
    }

    fun updateTokens(context: Context, accessToken: String, refreshToken: String) {
        SdkPreferenceManager(context.applicationContext).saveTokens(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }

    fun openChat(context: Context, conversationId: String? = null) {
        check(initialized) { "FarmerChatSdk not initialized." }
        val ctx = context.applicationContext
        val prefs = SdkPreferenceManager(ctx)
        val targetConversationId = conversationId ?: config.conversationId

        if (prefs.hasTokens()) {
            FarmerChatActivity.launch(context, targetConversationId)
        } else {
            // Always open the chat. If init fails, the chat screen will show an error via
            // ensureTokensInternal() and let the user retry.
            sdkScope.launch {
                val deviceId = prefs.getDeviceId()
                    ?: Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
                try { fetchGuestTokens(prefs, deviceId, config.baseUrl) }
                catch (e: Exception) { Log.w(TAG, "Pre-launch init failed (chat will show error): ${e.message}") }
                withContext(Dispatchers.Main) {
                    FarmerChatActivity.launch(context, targetConversationId)
                }
            }
        }
    }

    /**
     * Ensures tokens (including user_id) are saved before opening the chat overlay.
     * Throws [AuthInitException] if initialization fails so the ViewModel can show a
     * meaningful error to the user rather than a cryptic API failure.
     */
    internal suspend fun ensureTokensInternal() {
        val ctx = appContext ?: return
        val prefs = SdkPreferenceManager(ctx)
        Log.d(TAG, "ensureTokensInternal() — hasTokens=${prefs.hasTokens()} userId=${prefs.getUserId()}")
        if (prefs.hasTokens()) return

        initMutex.withLock {
            if (prefs.hasTokens()) return
            val deviceId = prefs.getDeviceId()
                ?: Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
            try {
                fetchGuestTokens(prefs, deviceId, config.baseUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Guest init failed: ${e.message}", e)
                throw AuthInitException(buildAuthErrorMessage(e.message))
            }
        }
    }

    /**
     * Calls initialize_user and saves tokens. Throws IOException on failure.
     * Callers are responsible for wrapping with user-friendly error handling.
     */
    private suspend fun fetchGuestTokens(
        prefs: SdkPreferenceManager,
        deviceId: String,
        baseUrl: String
    ) {
        withContext(Dispatchers.IO) {
            val response = GuestApiClient.initializeUser(baseUrl, deviceId)
            if (response.user_id.isNullOrEmpty()) {
                Log.e(TAG, "initialize_user returned null/empty user_id!")
                throw java.io.IOException("initialize_user returned empty user_id")
            }
            prefs.saveTokens(
                accessToken = response.access_token,
                refreshToken = response.refresh_token,
                userId = response.user_id
            )
            Log.d(TAG, "Tokens saved — userId=${response.user_id} createdNow=${response.created_now}")
        }
    }

    private fun buildAuthErrorMessage(rawMessage: String?): String {
        val msg = rawMessage ?: ""
        return when {
            // HTTP 4xx + device/limit keywords → permanent device limit error
            msg.contains("HTTP 4", ignoreCase = false) &&
                    (msg.contains("device", ignoreCase = true) || msg.contains("limit", ignoreCase = true)) ->
                "This device has reached its guest account limit. Please contact support."
            // Any other HTTP 4xx → backend rejected the request
            msg.contains("HTTP 4", ignoreCase = false) ->
                "Unable to start a chat session. Please contact support if this persists."
            // Fallback → likely a network/timeout error
            else ->
                "Please check your internet connection and try again."
        }
    }

    fun isInitialized(): Boolean = initialized

    fun shutdown() {
        SdkKoinHolder.stop()
        _config = null
        appContext = null
        initialized = false
    }

    fun clearSession(context: Context) {
        SdkPreferenceManager(context.applicationContext).clear()
    }
}

/**
 * Validates the SDK API key format.
 * Valid keys: fc_live_<16+ alphanumeric> or fc_test_<16+ alphanumeric>
 */
internal fun isValidSdkApiKey(key: String): Boolean {
    val prefix = when {
        key.startsWith("fc_live_") -> "fc_live_"
        key.startsWith("fc_test_") -> "fc_test_"
        else -> return false
    }
    val suffix = key.removePrefix(prefix)
    return suffix.length >= 16 && suffix.all { it.isLetterOrDigit() }
}
