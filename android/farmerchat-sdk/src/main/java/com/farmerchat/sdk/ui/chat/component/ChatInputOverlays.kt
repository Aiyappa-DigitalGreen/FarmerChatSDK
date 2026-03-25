package com.farmerchat.sdk.ui.chat.component

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.components.PrimaryInputButtons
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors

@Composable
internal fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCameraClick: () -> Unit,
    onMicClick: () -> Unit,
    selectedImageUri: Uri?,
    onClearImage: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val extColors = LocalSdkExtendedColors.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val hintText = config?.inputHintText ?: "Ask about your crops..."

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = extColors.inputBarBackground
    ) {
        Column {
            // Image preview
            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                selectedImageUri?.let { uri ->
                    Box(modifier = Modifier.padding(start = 16.dp, top = 10.dp)) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = onClearImage,
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove image",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 12.dp, top = 10.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pill-shaped text field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    TextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        placeholder = {
                            Text(
                                text = hintText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(onSend = { if (!isLoading) onSend() }),
                        maxLines = 4,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                PrimaryInputButtons(
                    hasText = text.isNotBlank() || selectedImageUri != null,
                    isLoading = isLoading,
                    onSendClick = onSend,
                    onCameraClick = onCameraClick,
                    onMicClick = onMicClick
                )
            }
        }
    }
}

@Composable
internal fun ImageSourcePickerSheet(
    onCameraSelected: () -> Unit,
    onGallerySelected: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Add Photo",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onCameraSelected(); onDismiss() },
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = "Camera",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Camera", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { onGallerySelected(); onDismiss() },
                        modifier = Modifier.size(68.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text("Gallery", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface)
            }
        }
        Spacer(Modifier.height(12.dp))
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
