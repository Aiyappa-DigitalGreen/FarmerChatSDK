package com.farmerchat.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.preference.SdkPreferenceManager
import com.farmerchat.sdk.ui.chat.ChatScreen
import com.farmerchat.sdk.ui.history.HistoryScreen
import com.farmerchat.sdk.ui.language.LanguageSelectionScreen

private object Routes {
    const val LANGUAGE   = "language"
    const val CHAT       = "chat"
    const val HISTORY    = "history"
    const val CHAT_FROM_HISTORY = "chat/{conversationId}"
    fun chatFromHistory(conversationId: String) = "chat/$conversationId"
}

/**
 * The full FarmerChat navigation graph.
 * Rendered inside a Dialog by FarmerChatFab (same-screen overlay),
 * or inside FarmerChatActivity for the legacy full-screen path.
 */
@Composable
internal fun FarmerChatNavHost(
    startConversationId: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val prefs  = SdkPreferenceManager(context)
    val showLanguage = config?.showLanguageSelection == true && !prefs.hasSelectedLanguage()
    val startDest = if (showLanguage) Routes.LANGUAGE else Routes.CHAT

    NavHost(
        navController = navController,
        startDestination = startDest,
        modifier = modifier
    ) {
        composable(Routes.LANGUAGE) {
            LanguageSelectionScreen(
                onLanguageSelected = {
                    navController.navigate(Routes.CHAT) {
                        popUpTo(Routes.LANGUAGE) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CHAT) {
            ChatScreen(
                conversationId = startConversationId,
                onNavigateToHistory = {
                    FarmerChatSdk.config.analyticsListener?.onHistoryOpened()
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateUp = onClose
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onNavigateUp = { navController.popBackStack() },
                onConversationSelected = { conversationId ->
                    navController.navigate(Routes.chatFromHistory(conversationId)) {
                        popUpTo(Routes.HISTORY) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Routes.CHAT_FROM_HISTORY,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId")
            ChatScreen(
                conversationId = conversationId,
                onNavigateToHistory = {
                    FarmerChatSdk.config.analyticsListener?.onHistoryOpened()
                    navController.navigate(Routes.HISTORY)
                },
                onNavigateUp = { if (!navController.popBackStack()) onClose() }
            )
        }
    }
}
