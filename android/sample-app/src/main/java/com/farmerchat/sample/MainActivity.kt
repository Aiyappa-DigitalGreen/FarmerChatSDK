package com.farmerchat.sample

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Camera
import androidx.compose.material.icons.outlined.EmojiObjects
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatConfig
import com.farmerchat.sdk.FarmerChatFab
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.api.SdkAnalyticsListener

private val DarkBg = Color(0xFF0D1A0B)
private val CardBg = Color(0xFF172213)
private val Green = Color(0xFF4CAF50)
private val GreenDim = Color(0xFF2E7D32)
private val TextMuted = Color(0xFF8A9E88)
private val TextWhite = Color(0xFFEEF2ED)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FarmerChatSdk.initialize(
            context = this,
            config = FarmerChatConfig(
                sdkApiKey = "fc_test_samplekey1234567890",
                baseUrl = "https://dev.efarm.digitalgreen.org/be/",
                showLanguageSelection = true,
                countryCode = "IN"
            )
        )

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = GreenDim,
                    secondary = Green
                )
            ) {
                AboutScreen()
            }
        }
    }
}

@Composable
private fun AboutScreen() {
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FarmerChatFab(extended = true, label = "Ask FarmerChat")
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            // Logo circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Green),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 32.sp)
            }

            Spacer(Modifier.height(16.dp))

            // App name
            Text(
                text = "FarmChat AI",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Subtitle
            Text(
                text = "Your Smart Farming Assistant",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            // Version badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(CardBg)
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Version 1.0",
                    fontSize = 12.sp,
                    color = Green,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(Modifier.height(32.dp))

            // What is section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "What is FarmChat AI?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "FarmChat AI is your personal farming assistant powered by artificial intelligence. Ask any agriculture question using voice, text, or images — and get instant expert advice in your local language.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    lineHeight = 22.sp
                )
            }

            Spacer(Modifier.height(28.dp))

            // Features label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FEATURES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            // 2×2 feature cards grid
            val features = listOf(
                FeatureCard(Icons.Outlined.Mic, "Voice First", "Just speak your question — no typing needed"),
                FeatureCard(Icons.Outlined.Camera, "Crop Scan", "Take a photo to detect crop diseases instantly"),
                FeatureCard(Icons.Outlined.Language, "Local Languages", "Get answers in Hindi, Kannada, Tamil & more"),
                FeatureCard(Icons.Outlined.EmojiObjects, "Expert Advice", "AI trained on agriculture data for accurate tips")
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(0.dp),
                userScrollEnabled = false
            ) {
                items(features) { feature ->
                    FeatureCardItem(feature)
                }
            }

            Spacer(Modifier.height(100.dp))
        }
    }
}

private data class FeatureCard(
    val icon: ImageVector,
    val title: String,
    val description: String
)

@Composable
private fun FeatureCardItem(feature: FeatureCard) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg)
            .padding(14.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Green.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = feature.title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = feature.description,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )
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
