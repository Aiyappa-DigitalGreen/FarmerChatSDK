package com.farmerchat.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors

@Composable
internal fun SuggestedQuestionsSection(
    questions: List<String>,
    onQuestionSelected: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val extColors = LocalSdkExtendedColors.current
    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val headerText = config?.followUpHeaderText ?: "Related questions"
    val showIcon = config?.showFollowUpHeaderIcon != false

    Column(modifier = modifier.fillMaxWidth().padding(top = 12.dp)) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        ) {
            if (showIcon) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(extColors.followUpButtonBackground.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = extColors.followUpButtonBackground,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
            Text(
                text = headerText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }

        // Horizontal scrollable pill chips
        LazyRow(
            contentPadding = PaddingValues(start = 2.dp, end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(questions.take(5)) { index, question ->
                FollowUpPill(
                    question = question,
                    background = extColors.followUpCardBackground,
                    textColor = extColors.followUpText,
                    accentColor = extColors.followUpButtonBackground,
                    onClick = { onQuestionSelected(question, index) }
                )
            }
        }

        Spacer(Modifier.height(4.dp))
    }
}

@Composable
private fun FollowUpPill(
    question: String,
    background: Color,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .widthIn(min = 140.dp, max = 220.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(50.dp),
                spotColor = accentColor.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(50.dp))
            .background(background)
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.18f),
                shape = RoundedCornerShape(50.dp)
            )
            .clickable(onClick = onClick)
            .padding(start = 14.dp, end = 10.dp, top = 9.dp, bottom = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = question,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(accentColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Send,
                contentDescription = "Ask",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
