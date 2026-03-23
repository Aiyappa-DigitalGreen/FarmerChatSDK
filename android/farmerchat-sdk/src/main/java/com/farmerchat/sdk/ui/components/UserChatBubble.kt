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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors

@Composable
internal fun UserChatBubble(
    text: String,
    imageUri: Uri? = null,
    audioUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    val extColors = LocalSdkExtendedColors.current
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.78).dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 4.dp,
                        bottomStart = 16.dp,
                        bottomEnd = 16.dp
                    )
                )
                .background(extColors.userBubbleBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Text
            if (text.isNotBlank()) {
                Text(
                    text = text,
                    color = extColors.userBubbleText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
