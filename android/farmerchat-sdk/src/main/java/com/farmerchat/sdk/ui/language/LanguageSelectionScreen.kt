package com.farmerchat.sdk.ui.language

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.draw.alpha
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.base.UiState
import com.farmerchat.sdk.domain.model.language.SupportedLanguage
import com.farmerchat.sdk.domain.model.language.SupportedLanguageGroup
import com.farmerchat.sdk.ui.theme.SdkDarkBg
import com.farmerchat.sdk.ui.theme.SdkGreen500
import com.farmerchat.sdk.ui.theme.SdkTextSecondary
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    viewModel: LanguageViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isSubmitting = state.submitState is UiState.Loading

    val headerAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        headerAlpha.animateTo(1f, animationSpec = tween(600))
    }

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
            Spacer(Modifier.height(56.dp))

            // Animated logo
            val logoScale by animateFloatAsState(
                targetValue = if (headerAlpha.value > 0.5f) 1f else 0.6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "logoScale"
            )
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(SdkGreen500),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 36.sp)
            }

            Spacer(Modifier.height(20.dp))

            AnimatedVisibility(
                visible = headerAlpha.value > 0.3f,
                enter = fadeIn(tween(500)) + slideInVertically { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FarmerChat AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                    Text(
                        text = "Choose your preferred language",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SdkTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            AnimatedVisibility(
                visible = headerAlpha.value > 0.5f,
                enter = fadeIn(tween(400))
            ) {
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
            }

            Spacer(Modifier.height(14.dp))

            // Content based on state
            Box(modifier = Modifier.weight(1f)) {
                when (val langState = state.languageState) {
                    is UiState.Loading, UiState.Idle -> {
                        LanguageLoadingContent()
                    }
                    is UiState.Error -> {
                        LanguageErrorContent(
                            message = langState.message ?: "Failed to load languages",
                            onRetry = { viewModel.fetchLanguages() }
                        )
                    }
                    is UiState.Success -> {
                        val allLanguages = langState.data.flatMap { it.languages }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(allLanguages) { index, lang ->
                                val itemAlpha = remember { Animatable(0f) }
                                LaunchedEffect(lang.id) {
                                    kotlinx.coroutines.delay(index * 60L)
                                    itemAlpha.animateTo(1f, tween(300))
                                }
                                LanguageCard(
                                    language = lang,
                                    isSelected = state.selectedLanguageId == lang.id,
                                    alpha = itemAlpha.value,
                                    onClick = { viewModel.selectLanguage(lang) }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Continue button
            val canContinue = state.selectedLanguageId != null &&
                    state.languageState is UiState.Success &&
                    !isSubmitting

            AnimatedVisibility(
                visible = headerAlpha.value > 0.7f,
                enter = fadeIn(tween(500)) + slideInVertically { 60 }
            ) {
                Button(
                    onClick = { viewModel.submitLanguage(onSuccess = onLanguageSelected) },
                    enabled = canContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SdkGreen500,
                        disabledContainerColor = SdkGreen500.copy(alpha = 0.4f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
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
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LanguageLoadingContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1A2318).copy(alpha = shimmerAlpha))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            )
        }
    }
}

@Composable
private fun LanguageErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            fontSize = 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = SdkTextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(
                text = "Retry",
                color = SdkGreen500,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    isSelected: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.03f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    val borderColor = if (isSelected) SdkGreen500 else Color.White.copy(alpha = 0.1f)
    val bgColor = if (isSelected) SdkGreen500.copy(alpha = 0.15f) else Color(0xFF1A2318)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor.copy(alpha = bgColor.alpha * alpha))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor.copy(alpha = borderColor.alpha * alpha),
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
            val displayName = language.displayName.ifBlank { language.name }
            val nativeName = language.name

            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = alpha),
                fontSize = 13.sp
            )
            if (nativeName != displayName) {
                Text(
                    text = nativeName,
                    style = MaterialTheme.typography.bodySmall,
                    color = SdkTextSecondary.copy(alpha = SdkTextSecondary.alpha * alpha),
                    fontSize = 11.sp
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
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

        if (!isSelected) {
            Spacer(Modifier.width(20.dp))
        }
    }
}

// LazyGrid items helper (no key version for shimmer)
private fun androidx.compose.foundation.lazy.grid.LazyGridScope.items(
    count: Int,
    itemContent: @Composable androidx.compose.foundation.lazy.grid.LazyGridItemScope.(index: Int) -> Unit
) {
    repeat(count) { index ->
        item { itemContent(index) }
    }
}
