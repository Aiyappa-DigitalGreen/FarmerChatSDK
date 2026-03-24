package com.farmerchat.sdk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.farmerchat.sdk.FarmerChatSdk

private val LightColorScheme = lightColorScheme(
    primary = SdkGreen800,
    onPrimary = Color.White,
    primaryContainer = SdkGreen100,
    onPrimaryContainer = SdkGreen900,
    secondary = SdkGreen500,
    onSecondary = Color.White,
    secondaryContainer = SdkGreen50,
    onSecondaryContainer = SdkGreen900,
    tertiary = SdkGreen600,
    background = SdkBackground,
    onBackground = SdkOnSurface,
    surface = SdkSurface,
    onSurface = SdkOnSurface,
    error = SdkError,
    errorContainer = SdkErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    primary = SdkGreenDark,
    onPrimary = SdkGreen900,
    primaryContainer = SdkGreen800,
    onPrimaryContainer = SdkGreen100,
    secondary = SdkGreen400,
    onSecondary = SdkGreen900,
    secondaryContainer = SdkGreen700,
    onSecondaryContainer = SdkGreen100,
    tertiary = SdkGreen300,
    background = SdkBackgroundDark,
    onBackground = Color(0xFFE6E1E5),
    surface = SdkSurfaceDark,
    onSurface = Color(0xFFE6E1E5),
    error = Color(0xFFCF6679)
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
        userBubbleBackground = SdkUserBubble,
        userBubbleText = SdkUserBubbleText,
        aiBubbleBackground = SdkAiBubble,
        aiBubbleText = SdkAiBubbleText,
        topBarBackground = SdkSurface,
        topBarTitle = SdkOnSurface,
        topBarSubtitle = SdkGreen800,
        chatBackground = SdkBackground,
        aiAvatarBackground = SdkGreen100,
        inputBarBackground = SdkSurface,
        sendButtonBackground = SdkGreen800,
        followUpCardBackground = Color(0xFFF5F5F5),
        followUpText = SdkOnSurface,
        followUpButtonBackground = SdkGreen800,
        followUpButtonText = Color.White
    )
}

@Composable
internal fun SdkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val primaryColor = config?.primaryColor?.let { Color(it) } ?: SdkGreen800

    val baseScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = baseScheme.copy(
        primary = if (darkTheme) baseScheme.primary else primaryColor,
        secondary = if (darkTheme) baseScheme.secondary else primaryColor,
        tertiary = if (darkTheme) baseScheme.tertiary else primaryColor
    )

    val extendedColors = if (darkTheme) {
        SdkExtendedColors(
            userBubbleBackground = config?.userBubbleColor?.let { Color(it) } ?: SdkGreen700,
            userBubbleText       = config?.userBubbleTextColor?.let { Color(it) } ?: Color.White,
            aiBubbleBackground   = config?.aiBubbleColor?.let { Color(it) } ?: SdkAiBubbleDark,
            aiBubbleText         = config?.aiBubbleTextColor?.let { Color(it) } ?: SdkAiBubbleTextDark,
            topBarBackground     = config?.topBarBackgroundColor?.let { Color(it) } ?: SdkSurfaceDark,
            topBarTitle          = config?.topBarTitleColor?.let { Color(it) } ?: Color(0xFFE6E1E5),
            topBarSubtitle       = config?.topBarSubtitleColor?.let { Color(it) } ?: SdkGreenDark,
            chatBackground       = config?.chatBackgroundColor?.let { Color(it) } ?: SdkBackgroundDark,
            aiAvatarBackground   = config?.aiAvatarBackgroundColor?.let { Color(it) } ?: SdkGreen800,
            inputBarBackground   = config?.inputBarBackgroundColor?.let { Color(it) } ?: SdkSurfaceDark,
            sendButtonBackground = config?.sendButtonColor?.let { Color(it) } ?: SdkGreenDark,
            followUpCardBackground = config?.followUpCardBackgroundColor?.let { Color(it) } ?: Color(0xFF2C3E2D),
            followUpText         = config?.followUpTextColor?.let { Color(it) } ?: Color(0xFFE8F5E9),
            followUpButtonBackground = config?.followUpButtonColor?.let { Color(it) } ?: SdkGreenDark,
            followUpButtonText   = config?.followUpButtonTextColor?.let { Color(it) } ?: Color.White
        )
    } else {
        SdkExtendedColors(
            userBubbleBackground = config?.userBubbleColor?.let { Color(it) } ?: SdkUserBubble,
            userBubbleText       = config?.userBubbleTextColor?.let { Color(it) } ?: SdkUserBubbleText,
            aiBubbleBackground   = config?.aiBubbleColor?.let { Color(it) } ?: SdkAiBubble,
            aiBubbleText         = config?.aiBubbleTextColor?.let { Color(it) } ?: SdkAiBubbleText,
            topBarBackground     = config?.topBarBackgroundColor?.let { Color(it) } ?: primaryColor,
            topBarTitle          = config?.topBarTitleColor?.let { Color(it) } ?: Color.White,
            topBarSubtitle       = config?.topBarSubtitleColor?.let { Color(it) } ?: Color.White.copy(alpha = 0.82f),
            chatBackground       = config?.chatBackgroundColor?.let { Color(it) } ?: SdkBackground,
            aiAvatarBackground   = config?.aiAvatarBackgroundColor?.let { Color(it) } ?: SdkGreen100,
            inputBarBackground   = config?.inputBarBackgroundColor?.let { Color(it) } ?: SdkSurface,
            sendButtonBackground = config?.sendButtonColor?.let { Color(it) } ?: primaryColor,
            followUpCardBackground = config?.followUpCardBackgroundColor?.let { Color(it) } ?: Color(0xFFF5F5F5),
            followUpText         = config?.followUpTextColor?.let { Color(it) } ?: SdkOnSurface,
            followUpButtonBackground = config?.followUpButtonColor?.let { Color(it) } ?: primaryColor,
            followUpButtonText   = config?.followUpButtonTextColor?.let { Color(it) } ?: Color.White
        )
    }

    CompositionLocalProvider(LocalSdkExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SdkTypography,
            content = content
        )
    }
}
