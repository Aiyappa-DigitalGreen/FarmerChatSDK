package com.farmerchat.sdk.utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import org.json.JSONObject
import java.net.URLEncoder

internal fun Context.getEncodedDeviceConfig(): String {
    val androidId = try {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "N/A"
    } catch (e: Exception) {
        "N/A"
    }
    val deviceConfig = mapOf(
        "Build-Version" to "v2",
        "app_version_name" to try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            "1.0"
        },
        "manufacturer" to Build.MANUFACTURER.replaceFirstChar { it.titlecase() },
        "model" to Build.MODEL,
        "brand" to Build.BRAND,
        "android_sdk_version" to Build.VERSION.SDK_INT,
        "os_version" to Build.VERSION.RELEASE,
        "android_id" to androidId
    )
    return URLEncoder.encode(JSONObject(deviceConfig).toString(), "UTF-8")
}
