package com.farmerchat.sdk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.HorizontalDivider
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

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
            thickness = 0.5.dp
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                if (showIcon) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = extColors.followUpButtonBackground.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(5.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lightbulb,
                            contentDescription = null,
                            tint = extColors.followUpButtonBackground,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Text(
                    text = headerText.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.6.sp,
                    fontSize = 10.sp
                )
            }

            // Horizontal scrollable chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(questions.take(5)) { index, question ->
                    FollowUpChip(
                        question = question,
                        chipBackground = extColors.followUpCardBackground,
                        textColor = extColors.followUpText,
                        accentColor = extColors.followUpButtonBackground,
                        onClick = { onQuestionSelected(question, index) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowUpChip(
    question: String,
    chipBackground: Color,
    textColor: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .widthIn(min = 130.dp, max = 210.dp)
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(14.dp),
                spotColor = accentColor.copy(alpha = 0.14f),
                ambientColor = accentColor.copy(alpha = 0.06f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(chipBackground)
            .clickable(onClick = onClick)
    ) {
        // Left accent bar
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .fillMaxHeight()
                .background(
                    color = accentColor,
                    shape = RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                )
        )
        Column(
            modifier = Modifier.padding(
                start = 10.dp, end = 12.dp, top = 10.dp, bottom = 10.dp
            )
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                maxLines = 3,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "Tap to ask →",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
