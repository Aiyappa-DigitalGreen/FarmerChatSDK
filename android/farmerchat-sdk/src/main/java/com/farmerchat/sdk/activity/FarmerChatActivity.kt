package com.farmerchat.sdk.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.farmerchat.sdk.di.SdkKoinHolder
import com.farmerchat.sdk.ui.FarmerChatNavHost
import com.farmerchat.sdk.ui.theme.SdkTheme
import org.koin.compose.KoinContext

/**
 * Kept for host apps that prefer to open FarmerChat as a full-screen Activity
 * rather than using the in-app Dialog overlay in FarmerChatFab.
 */
class FarmerChatActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val conversationId = intent.getStringExtra(EXTRA_CONVERSATION_ID)

        setContent {
            KoinContext(context = SdkKoinHolder.koin) {
                SdkTheme {
                    FarmerChatNavHost(
                        startConversationId = conversationId,
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    companion object {
        private const val EXTRA_CONVERSATION_ID = "extra_conversation_id"

        fun launch(context: Context, conversationId: String?) {
            val intent = Intent(context, FarmerChatActivity::class.java).apply {
                conversationId?.let { putExtra(EXTRA_CONVERSATION_ID, it) }
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            context.startActivity(intent)
        }
    }
}
