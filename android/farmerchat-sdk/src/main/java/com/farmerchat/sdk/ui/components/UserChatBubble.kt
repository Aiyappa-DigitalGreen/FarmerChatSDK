package com.farmerchat.sdk.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors

@Composable
internal fun UserChatBubble(
    text: String,
    imageUri: Uri? = null,
    audioUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    val extColors = LocalSdkExtendedColors.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.78).dp

    val r = (config?.bubbleCornerRadius ?: 16f).dp
    val fontSize = (config?.messageFontSizeSp ?: 14f).sp
    val showAvatar = config?.showUserAvatar != false

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // User avatar
        if (showAvatar) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = extColors.userBubbleBackground.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "User",
                        tint = extColors.userBubbleBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = r,
                        bottomStart = r,
                        bottomEnd = r
                    )
                )
                .background(extColors.userBubbleBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Image attachment
            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                if (text.isNotBlank()) Spacer(Modifier.height(6.dp))
            }

            // Audio indicator
            if (audioUri != null && text.isBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Voice message",
                        tint = extColors.userBubbleText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Voice message",
                        color = extColors.userBubbleText,
                        fontSize = fontSize,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Text
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    color = extColors.userBubbleText,
                    fontSize = fontSize,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
