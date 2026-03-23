package com.farmerchat.sdk.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.chat.ChatScreen
import com.farmerchat.sdk.ui.history.HistoryScreen

private object Routes {
    const val CHAT = "chat"
    const val HISTORY = "history"
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
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CHAT,
        modifier = modifier
    ) {
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
                    // Pop HISTORY off the stack so pressing back from the loaded
                    // conversation returns to the main chat screen, not closes the SDK.
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
                // Nothing below on the stack — back always closes the SDK
                onNavigateUp = { if (!navController.popBackStack()) onClose() }
            )
        }
    }
}
