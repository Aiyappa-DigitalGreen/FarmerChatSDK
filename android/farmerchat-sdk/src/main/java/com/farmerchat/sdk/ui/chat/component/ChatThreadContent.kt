package com.farmerchat.sdk.ui.chat.component

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
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
    // Auto-scroll to bottom on new messages
    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    val lastAiMessageId = state.messages.filterIsInstance<ChatMessage.AiResponse>().lastOrNull()?.id

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        reverseLayout = false
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
                    ChatLoadingContent(
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Inline error at bottom
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
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.88).dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Bot avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    Modifier.padding(0.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(
                        text = "🌱",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier.widthIn(max = maxWidth)
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 4.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = 16.dp
                ),
                color = extColors.aiBubbleBackground,
                tonalElevation = 1.dp
            ) {
                MarkdownText(
                    markdown = message.text,
                    color = extColors.aiBubbleText,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }

            // Actions row
            ChatResponseActions(
                responseText = message.text,
                messageId = message.messageId,
                isListenLoading = isListenLoading,
                isAudioPlaying = isAudioPlaying,
                onListenClick = onListenClick
            )

            // Follow-up questions — only under the last AI response
            if (isLastAiMessage && !message.followUpQuestions.isNullOrEmpty()) {
                // Prefer state-level IDs (set after live queries); fall back to IDs baked
                // into the message itself (loaded from chat history).
                val ids = suggestedQuestionIds ?: message.followUpQuestionIds
                SuggestedQuestionsSection(
                    questions = message.followUpQuestions,
                    onQuestionSelected = { question, index ->
                        val questionId = ids?.getOrNull(index)
                        onFollowUpSelected(question, questionId)
                    }
                )
            }
        }
    }
}
