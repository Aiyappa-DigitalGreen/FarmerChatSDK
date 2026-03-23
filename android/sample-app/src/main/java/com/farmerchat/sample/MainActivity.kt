package com.farmerchat.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.farmerchat.sdk.FarmerChatConfig
import com.farmerchat.sdk.FarmerChatFab
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.api.SdkAnalyticsListener

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // SDK self-manages authentication via initialize_user API.
        // Only the baseUrl is required from the host app.
        FarmerChatSdk.initialize(
            context = this,
            config = FarmerChatConfig(
                sdkApiKey = "fc_test_samplekey1234567890",
                baseUrl = "https://dev.efarm.digitalgreen.org/be/"
            )
        )

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF2E7D32),
                    secondary = Color(0xFF4CAF50)
                )
            ) {
                SampleApp()
            }
        }
    }
}

@Composable
private fun SampleApp() {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FarmerChatFab(
                extended = true,
                label = "Ask FarmerChat"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "FarmerChat SDK Sample",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Tap the FAB or buttons below to open FarmerChat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(32.dp))

            Button(onClick = { FarmerChatSdk.openChat(context) }) {
                Text("Open New Chat")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    FarmerChatSdk.openChat(
                        context = context,
                        conversationId = "your_existing_conversation_id"
                    )
                }
            ) {
                Text("Resume Existing Conversation")
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(context, XmlDemoActivity::class.java))
                }
            ) {
                Text("XML Layout Demo (FarmerChatFabView)")
            }
        }
    }
}

private class SampleAnalyticsListener : SdkAnalyticsListener {
    override fun onNewConversationCreated(conversationId: String) {
        Log.d("FarmerChatAnalytics", "New conversation: $conversationId")
    }
    override fun onTextQuerySent(query: String, conversationId: String, inputType: String) {
        Log.d("FarmerChatAnalytics", "Text query: \"$query\" in $conversationId")
    }
    override fun onImageQuerySent(conversationId: String) {
        Log.d("FarmerChatAnalytics", "Image query in: $conversationId")
    }
    override fun onVoiceQuerySent(conversationId: String) {
        Log.d("FarmerChatAnalytics", "Voice query in: $conversationId")
    }
    override fun onFollowUpQuestionTapped(question: String, questionId: String?) {
        Log.d("FarmerChatAnalytics", "Follow-up: \"$question\"")
    }
    override fun onListenButtonTapped(messageId: String) {
        Log.d("FarmerChatAnalytics", "Listen tapped: $messageId")
    }
    override fun onApiError(apiName: String, errorCode: Int?, errorMessage: String?) {
        Log.e("FarmerChatAnalytics", "API error [$apiName] $errorCode: $errorMessage")
    }
    override fun onHistoryOpened() {
        Log.d("FarmerChatAnalytics", "History opened")
    }
    override fun onHistoryConversationSelected(conversationId: String) {
        Log.d("FarmerChatAnalytics", "History selected: $conversationId")
    }
}
