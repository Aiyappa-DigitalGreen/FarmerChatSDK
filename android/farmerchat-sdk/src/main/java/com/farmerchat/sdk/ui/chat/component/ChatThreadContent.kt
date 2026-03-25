package com.farmerchat.sdk.ui.chat.component

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.chat.model.ChatMessage
import com.farmerchat.sdk.ui.chat.udf.ChatState
import com.farmerchat.sdk.ui.components.MarkdownText
import com.farmerchat.sdk.ui.components.SuggestedQuestionsSection
import com.farmerchat.sdk.ui.components.UserChatBubble
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors
import com.farmerchat.sdk.ui.theme.SdkGreen500
import com.farmerchat.sdk.ui.theme.SdkTextSecondary
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun ChatThreadContent(
    state: ChatState,
    listState: LazyListState = rememberLazyListState(),
    onFollowUpSelected: (String, String?) -> Unit,
    onListenClick: () -> Unit,
    onRetry: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val lastAiMessageId = state.messages.filterIsInstance<ChatMessage.AiResponse>().lastOrNull()?.id

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(state.messages, key = { it.id }) { message ->
            when (message) {
                is ChatMessage.UserMessage -> {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(tween(280)) { it / 2 } + fadeIn(tween(280)),
                        modifier = Modifier.animateItem(tween(280))
                    ) {
                        // User bubble + timestamp below, right-aligned
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            UserChatBubble(
                                text = message.text,
                                imageUri = message.imageUri,
                                audioUri = message.audioUri
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = currentTimeString(),
                                fontSize = 10.sp,
                                color = Color(0xFF5A6B58)
                            )
                        }
                    }
                }

                is ChatMessage.AiResponse -> {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(tween(300)) { -it / 2 } + fadeIn(tween(300)),
                        modifier = Modifier.animateItem(tween(300))
                    ) {
                        AiResponseBubble(
                            message = message,
                            isLastAiMessage = message.id == lastAiMessageId,
                            isListenLoading = state.isLoadingSynthesiseAudio,
                            isAudioPlaying = state.isAudioPlaying,
                            onListenClick = onListenClick,
                            onFollowUpSelected = onFollowUpSelected,
                            suggestedQuestionIds = state.suggestedQuestionIds,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                is ChatMessage.LoadingPlaceholder -> {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(tween(250)) { it / 2 } + fadeIn(tween(250)),
                        modifier = Modifier.animateItem(tween(250))
                    ) {
                        ChatLoadingContent(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }

        if (state.errorMessage != null) {
            item {
                InlineErrorContent(
                    message = state.errorMessage,
                    onRetry = onRetry,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AiResponseBubble(
    message: ChatMessage.AiResponse,
    isLastAiMessage: Boolean,
    isListenLoading: Boolean,
    isAudioPlaying: Boolean,
    onListenClick: () -> Unit,
    onFollowUpSelected: (String, String?) -> Unit,
    suggestedQuestionIds: List<String>?,
    modifier: Modifier = Modifier
) {
    val extColors = LocalSdkExtendedColors.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.88).dp
    val r = (config?.bubbleCornerRadius ?: 18f).dp
    val fontSize = (config?.messageFontSizeSp ?: 14f)
    val avatarEmoji = config?.aiAvatarEmoji ?: "🌱"
    val aiName = config?.chatTitle ?: "FarmerChat AI"

    // Pointer at top-left for AI
    val bubbleShape = RoundedCornerShape(
        topStart = 4.dp, topEnd = r, bottomStart = r, bottomEnd = r
    )

    Column(modifier = modifier.fillMaxWidth()) {
        // Bubble constrained to maxWidth
        Box(modifier = Modifier.widthIn(max = maxWidth)) {
            // Card bubble with shadow elevation=6dp
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 6.dp,
                        shape = bubbleShape,
                        ambientColor = Color(0xFF1A2318).copy(alpha = 0.3f),
                        spotColor = Color(0xFF1A2318).copy(alpha = 0.4f)
                    )
                    .clip(bubbleShape)
                    .background(Color(0xFF1A2318))
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    // Header row: 22dp green circle + "FarmerChat AI" green semibold + Spacer + timestamp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(SdkGreen500),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = avatarEmoji, fontSize = 11.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = aiName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SdkGreen500,
                            fontSize = 11.sp
                        )
                        Spacer(Modifier.weight(1f))
                        // Timestamp on same line as name, right side
                        Text(
                            text = currentTimeString(),
                            fontSize = 10.sp,
                            color = Color(0xFF5A6B58)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    MarkdownText(
                        markdown = message.text,
                        color = Color(0xFFDCE8DA),
                        fontSize = fontSize,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Actions row — full width, not constrained
        ChatResponseActions(
            responseText = message.text,
            messageId = message.messageId,
            isListenLoading = isListenLoading,
            isAudioPlaying = isAudioPlaying,
            onListenClick = onListenClick
        )

        // Follow-up questions — full width so chips aren't clipped
        if (isLastAiMessage && !message.followUpQuestions.isNullOrEmpty()) {
            val ids = suggestedQuestionIds ?: message.followUpQuestionIds
            SuggestedQuestionsSection(
                questions = message.followUpQuestions,
                onQuestionSelected = { question, index ->
                    onFollowUpSelected(question, ids?.getOrNull(index))
                }
            )
        }

        Spacer(Modifier.height(2.dp))
    }
}

// Helper: returns current time as "h:mm a" string
private fun currentTimeString(): String {
    return try {
        LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    } catch (e: Exception) {
        ""
    }
}
