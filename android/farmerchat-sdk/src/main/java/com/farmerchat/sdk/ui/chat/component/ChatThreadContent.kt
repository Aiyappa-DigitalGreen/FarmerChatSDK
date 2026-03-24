package com.farmerchat.sdk.ui.chat.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
                    UserChatBubble(
                        text = message.text,
                        imageUri = message.imageUri,
                        audioUri = message.audioUri,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                is ChatMessage.AiResponse -> {
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

                is ChatMessage.LoadingPlaceholder -> {
                    ChatLoadingContent(modifier = Modifier.padding(vertical = 4.dp))
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
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.84).dp
    val r = (config?.bubbleCornerRadius ?: 20f).dp
    val fontSize = (config?.messageFontSizeSp ?: 15f)
    val avatarEmoji = config?.aiAvatarEmoji ?: "🌱"
    val avatarBg = extColors.aiAvatarBackground
    val primaryColor = MaterialTheme.colorScheme.primary

    // Pointer shape: all large corners except bottom-left (AI = left side)
    val bubbleShape = RoundedCornerShape(
        topStart = r,
        topEnd = r,
        bottomStart = 5.dp,
        bottomEnd = r
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Gradient avatar circle
        Box(
            modifier = Modifier
                .size(38.dp)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    spotColor = primaryColor.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.85f),
                            primaryColor.copy(alpha = 0.55f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = avatarEmoji,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.widthIn(max = maxWidth)) {
            // Sender name
            Text(
                text = (config?.chatTitle ?: "FarmerChat").uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 5.dp)
            )

            // White card bubble with real shadow
            Surface(
                shape = bubbleShape,
                color = extColors.aiBubbleBackground,
                shadowElevation = 6.dp,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = bubbleShape
                    )
            ) {
                MarkdownText(
                    markdown = message.text,
                    color = extColors.aiBubbleText,
                    fontSize = fontSize,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                )
            }

            // Actions
            ChatResponseActions(
                responseText = message.text,
                messageId = message.messageId,
                isListenLoading = isListenLoading,
                isAudioPlaying = isAudioPlaying,
                onListenClick = onListenClick
            )

            // Follow-up questions
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
}
