package com.farmerchat.sdk.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.farmerchat.sdk.FarmerChatSdk

// Always dark — SDK has a dark-green design identity
private val BaseDarkScheme = darkColorScheme(
    primary            = SdkGreen500,
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFF1F3A1D),
    onPrimaryContainer = SdkGreen300,
    secondary          = SdkGreen400,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF1A2E18),
    onSecondaryContainer = SdkGreen300,
    tertiary           = SdkGreenAccent,
    background         = SdkDarkBg,
    onBackground       = SdkTextPrimary,
    surface            = SdkDarkSurface,
    onSurface          = SdkTextPrimary,
    surfaceVariant     = SdkDarkSurface2,
    onSurfaceVariant   = SdkTextSecondary,
    outlineVariant     = Color(0xFF2E3F2C),
    error              = SdkError,
    errorContainer     = SdkErrorContainer,
    onError            = Color.White,
    onErrorContainer   = Color(0xFFFFB4AB)
)

/**
 * Extended color + style tokens provided via CompositionLocal.
 * Access via [LocalSdkExtendedColors].current inside any SDK composable.
 */
data class SdkExtendedColors(
    // Bubbles
    val userBubbleBackground: Color,
    val userBubbleText: Color,
    val aiBubbleBackground: Color,
    val aiBubbleText: Color,
    // Top bar
    val topBarBackground: Color,
    val topBarTitle: Color,
    val topBarSubtitle: Color,
    // Chat background
    val chatBackground: Color,
    // AI avatar
    val aiAvatarBackground: Color,
    // Input bar
    val inputBarBackground: Color,
    val sendButtonBackground: Color,
    // Follow-up questions
    val followUpCardBackground: Color,
    val followUpText: Color,
    val followUpButtonBackground: Color,
    val followUpButtonText: Color
)

val LocalSdkExtendedColors = staticCompositionLocalOf {
    SdkExtendedColors(
        userBubbleBackground   = SdkUserBubble,
        userBubbleText         = SdkUserBubbleText,
        aiBubbleBackground     = SdkAiBubble,
        aiBubbleText           = SdkAiBubbleText,
        topBarBackground       = SdkDarkBg,
        topBarTitle            = SdkTextPrimary,
        topBarSubtitle         = SdkTextSecondary,
        chatBackground         = SdkDarkBg,
        aiAvatarBackground     = SdkGreen500,
        inputBarBackground     = SdkDarkSurface,
        sendButtonBackground   = SdkGreen500,
        followUpCardBackground = SdkDarkSurface,
        followUpText           = SdkTextPrimary,
        followUpButtonBackground = SdkGreen500,
        followUpButtonText     = Color.White
    )
}

@Composable
internal fun SdkTheme(
    content: @Composable () -> Unit
) {
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val primaryColor = config?.primaryColor?.let { Color(it) } ?: SdkGreen500

    val colorScheme = BaseDarkScheme.copy(
        primary   = primaryColor,
        secondary = primaryColor,
        tertiary  = primaryColor
    )

    val extendedColors = SdkExtendedColors(
        userBubbleBackground   = config?.userBubbleColor?.let { Color(it) } ?: SdkUserBubble,
        userBubbleText         = config?.userBubbleTextColor?.let { Color(it) } ?: SdkUserBubbleText,
        aiBubbleBackground     = config?.aiBubbleColor?.let { Color(it) } ?: SdkAiBubble,
        aiBubbleText           = config?.aiBubbleTextColor?.let { Color(it) } ?: SdkAiBubbleText,
        topBarBackground       = config?.topBarBackgroundColor?.let { Color(it) } ?: SdkDarkBg,
        topBarTitle            = config?.topBarTitleColor?.let { Color(it) } ?: SdkTextPrimary,
        topBarSubtitle         = config?.topBarSubtitleColor?.let { Color(it) } ?: SdkTextSecondary,
        chatBackground         = config?.chatBackgroundColor?.let { Color(it) } ?: SdkDarkBg,
        aiAvatarBackground     = config?.aiAvatarBackgroundColor?.let { Color(it) } ?: primaryColor,
        inputBarBackground     = config?.inputBarBackgroundColor?.let { Color(it) } ?: SdkDarkSurface,
        sendButtonBackground   = config?.sendButtonColor?.let { Color(it) } ?: primaryColor,
        followUpCardBackground = config?.followUpCardBackgroundColor?.let { Color(it) } ?: SdkDarkSurface,
        followUpText           = config?.followUpTextColor?.let { Color(it) } ?: SdkTextPrimary,
        followUpButtonBackground = config?.followUpButtonColor?.let { Color(it) } ?: primaryColor,
        followUpButtonText     = config?.followUpButtonTextColor?.let { Color(it) } ?: Color.White
    )

    CompositionLocalProvider(LocalSdkExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SdkTypography,
            content     = content
        )
    }
}
