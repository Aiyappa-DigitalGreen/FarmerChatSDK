package com.farmerchat.sdk.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.shadow
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
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.72).dp
    val r = (config?.bubbleCornerRadius ?: 20f).dp
    val fontSize = (config?.messageFontSizeSp ?: 15f).sp
    val bubbleColor = extColors.userBubbleBackground

    // Pointer shape: all large corners except bottom-right (user = right side)
    val bubbleShape = RoundedCornerShape(
        topStart = r,
        topEnd = r,
        bottomStart = r,
        bottomEnd = 5.dp
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,   // RIGHT-aligned
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .shadow(
                    elevation = 4.dp,
                    shape = bubbleShape,
                    ambientColor = bubbleColor.copy(alpha = 0.28f),
                    spotColor = bubbleColor.copy(alpha = 0.45f)
                )
                .clip(bubbleShape)
                .background(bubbleColor)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Image attachment
            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Attached image",
                    modifier = Modifier
                        .widthIn(max = 200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.FillWidth
                )
                if (text.isNotBlank()) Spacer(Modifier.height(8.dp))
            }

            // Audio indicator
            if (audioUri != null && text.isBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = null,
                        tint = extColors.userBubbleText.copy(alpha = 0.9f),
                        modifier = Modifier.size(15.dp)
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
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = (fontSize.value * 1.55f).sp
                )
            }
        }
    }
}
