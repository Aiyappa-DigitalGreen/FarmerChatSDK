package com.farmerchat.sdk.ui.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.preference.SdkPreferenceManager
import com.farmerchat.sdk.ui.theme.SdkDarkBg
import com.farmerchat.sdk.ui.theme.SdkDarkSurface
import com.farmerchat.sdk.ui.theme.SdkGreen500
import com.farmerchat.sdk.ui.theme.SdkTextSecondary

private data class Language(
    val englishName: String,
    val nativeName: String,
    val isDefault: Boolean = false
)

private val supportedLanguages = listOf(
    Language("English", "English", isDefault = true),
    Language("Hindi", "हिन्दी"),
    Language("Kannada", "ಕನ್ನಡ"),
    Language("Tamil", "தமிழ்"),
    Language("Telugu", "తెలుగు"),
    Language("Marathi", "मराठी")
)

@Composable
internal fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    var selectedLanguage by remember { mutableStateOf("English") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A2E10),
                        SdkDarkBg,
                        Color(0xFF0A1208)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            // Logo circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SdkGreen500),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = config?.aiAvatarEmoji ?: "🌱",
                    fontSize = 32.sp
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = config?.chatTitle ?: "FarmerChat AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 26.sp
            )
            Text(
                text = config?.chatSubtitle ?: "Smart Farming Assistant",
                style = MaterialTheme.typography.bodyMedium,
                color = SdkTextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(36.dp))

            // Section label
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SELECT YOUR LANGUAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SdkGreen500,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            // Language grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(supportedLanguages) { lang ->
                    LanguageCard(
                        language = lang,
                        isSelected = selectedLanguage == lang.englishName,
                        onClick = { selectedLanguage = lang.englishName }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // Continue button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(SdkGreen500)
                    .clickable {
                        SdkPreferenceManager(context).saveSelectedLanguage(selectedLanguage)
                        onLanguageSelected(selectedLanguage)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LanguageCard(
    language: Language,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SdkGreen500 else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isSelected) SdkGreen500.copy(alpha = 0.15f) else SdkDarkSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            tint = if (isSelected) SdkGreen500 else SdkTextSecondary,
            modifier = Modifier.size(18.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            Text(
                text = language.englishName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 13.sp
            )
            if (language.nativeName != language.englishName) {
                Text(
                    text = language.nativeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SdkTextSecondary,
                    fontSize = 11.sp
                )
            } else {
                Text(
                    text = "Default",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) SdkGreen500 else SdkTextSecondary,
                    fontSize = 11.sp
                )
            }
        }
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SdkGreen500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
