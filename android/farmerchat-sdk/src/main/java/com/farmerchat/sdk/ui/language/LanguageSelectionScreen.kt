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
import androidx.compose.material.icons.filled.SignalWifiOff
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.base.UiState
import com.farmerchat.sdk.domain.model.language.SupportedLanguage
import com.farmerchat.sdk.ui.theme.SdkGreen500
import com.farmerchat.sdk.ui.theme.SdkTextSecondary
import org.koin.androidx.compose.koinViewModel

// ── Colors for the warm rice-paddy photo-feel background ────────────────────
private val SkyTop       = Color(0xFFB5733A)   // warm amber/orange sky
private val SkyMidAmber  = Color(0xFF8B5A2B)   // deeper amber
private val HorizonGreen = Color(0xFF4A6830)   // transition green-grey at horizon
private val FieldGreen   = Color(0xFF2A5018)   // rice field green
private val FieldDeep    = Color(0xFF1A3A0D)   // deeper green
private val GroundDark   = Color(0xFF0D1F08)   // near-black foreground

@Composable
internal fun LanguageSelectionScreen(
    onLanguageSelected: (String) -> Unit,
    viewModel: LanguageViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isSubmitting = state.submitState is UiState.Loading

    val headerAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { headerAlpha.animateTo(1f, tween(700)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind { drawFarmingBackground() }
    ) {
        // Dark scrim so content is readable over the "photo-like" background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.15f),
                        0.45f to Color.Black.copy(alpha = 0.25f),
                        0.70f to Color.Black.copy(alpha = 0.55f),
                        1.0f to Color.Black.copy(alpha = 0.78f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // Bouncy logo — 56dp green circle with seedling emoji
            val logoScale by animateFloatAsState(
                targetValue = if (headerAlpha.value > 0.4f) 1f else 0.5f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "logoScale"
            )
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .scale(logoScale)
                    .clip(CircleShape)
                    .background(SdkGreen500),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🌱", fontSize = 26.sp)
            }

            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = headerAlpha.value > 0.3f,
                enter = fadeIn(tween(500)) + slideInVertically { -20 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "FarmChat AI",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Smart Farming Assistant",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // "SELECT YOUR LANGUAGE" label — left-aligned, NO divider lines
            AnimatedVisibility(
                visible = headerAlpha.value > 0.5f,
                enter = fadeIn(tween(400))
            ) {
                Text(
                    text = "SELECT YOUR LANGUAGE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SdkGreen500,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.8.sp,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))

            // Language content
            Box(modifier = Modifier.weight(1f)) {
                when (val langState = state.languageState) {
                    is UiState.Loading, UiState.Idle -> LanguageShimmer()

                    is UiState.Error -> LanguageErrorContent(
                        message = friendlyError(langState.message),
                        onRetry = { viewModel.fetchLanguages() }
                    )

                    is UiState.Success -> {
                        val allLanguages = langState.data.flatMap { it.languages }
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(allLanguages) { index, lang ->
                                val itemAlpha = remember { Animatable(0f) }
                                LaunchedEffect(lang.id) {
                                    kotlinx.coroutines.delay(index * 55L)
                                    itemAlpha.animateTo(1f, tween(280))
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

            Spacer(Modifier.height(12.dp))

            // Continue button
            val canContinue = state.selectedLanguageId != null &&
                    state.languageState is UiState.Success && !isSubmitting

            AnimatedVisibility(
                visible = headerAlpha.value > 0.6f,
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
                        disabledContainerColor = SdkGreen500.copy(alpha = 0.35f)
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text(
                            text = "Continue →",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Warm rice-paddy Canvas background ───────────────────────────────────────

private fun DrawScope.drawFarmingBackground() {
    val w = size.width
    val h = size.height

    // Sky gradient — warm amber top to dark green bottom
    drawRect(
        brush = Brush.verticalGradient(
            0.0f  to SkyTop,
            0.15f to SkyMidAmber,
            0.30f to HorizonGreen,
            0.55f to FieldGreen,
            0.80f to FieldDeep,
            1.0f  to GroundDark
        )
    )

    // Mountain silhouette — two overlapping peaks
    val mountainPath = Path().apply {
        moveTo(0f, h * 0.48f)
        cubicTo(w * 0.15f, h * 0.28f, w * 0.28f, h * 0.20f, w * 0.38f, h * 0.32f)
        cubicTo(w * 0.45f, h * 0.25f, w * 0.55f, h * 0.18f, w * 0.62f, h * 0.30f)
        cubicTo(w * 0.72f, h * 0.22f, w * 0.85f, h * 0.16f, w, h * 0.34f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = mountainPath,
        brush = Brush.verticalGradient(
            startY = h * 0.15f,
            endY = h * 0.55f,
            colorStops = arrayOf(
                0.0f to Color(0xFF1A2A10),
                1.0f to Color(0xFF0D1A08)
            )
        )
    )

    // Rice terrace lines (horizontal arcs across the lower half)
    val terraceLineColor = Color(0xFF3D6025).copy(alpha = 0.6f)
    val terraceCount = 7
    for (i in 0 until terraceCount) {
        val yBase = h * (0.52f + i * 0.068f)
        val path = Path().apply {
            moveTo(0f, yBase)
            cubicTo(
                w * 0.25f, yBase - h * 0.025f * (1 - i * 0.08f),
                w * 0.75f, yBase + h * 0.02f * (1 - i * 0.06f),
                w, yBase - h * 0.01f
            )
        }
        drawPath(
            path = path,
            color = terraceLineColor,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 1.5f + i * 0.3f
            )
        )
        // Fill between terraces for depth — alternating slightly different greens
        if (i < terraceCount - 1) {
            val yNext = h * (0.52f + (i + 1) * 0.068f)
            val fillPath = Path().apply {
                moveTo(0f, yBase)
                cubicTo(
                    w * 0.25f, yBase - h * 0.025f * (1 - i * 0.08f),
                    w * 0.75f, yBase + h * 0.02f * (1 - i * 0.06f),
                    w, yBase - h * 0.01f
                )
                lineTo(w, yNext)
                cubicTo(
                    w * 0.75f, yNext + h * 0.02f,
                    w * 0.25f, yNext - h * 0.02f,
                    0f, yNext
                )
                close()
            }
            val fillColor = if (i % 2 == 0)
                Color(0xFF1E3A10).copy(alpha = 0.4f + i * 0.04f)
            else
                Color(0xFF2A4A18).copy(alpha = 0.35f + i * 0.04f)
            drawPath(path = fillPath, color = fillColor)
        }
    }

    // Soft warm glow at horizon (sun setting)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFD4884A).copy(alpha = 0.22f),
                Color.Transparent
            ),
            center = Offset(w * 0.5f, h * 0.30f),
            radius = w * 0.55f
        ),
        radius = w * 0.55f,
        center = Offset(w * 0.5f, h * 0.30f)
    )
}

// ── Shimmer loading ──────────────────────────────────────────────────────────

@Composable
private fun LanguageShimmer() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "shimmerAlpha"
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = alpha * 0.45f))
                    .border(1.dp, Color.White.copy(alpha = alpha * 0.12f), RoundedCornerShape(14.dp))
            )
        }
    }
}

// ── Error state ──────────────────────────────────────────────────────────────

@Composable
private fun LanguageErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SignalWifiOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = SdkGreen500),
            shape = RoundedCornerShape(20.dp)
        ) {
            Text("Try Again", color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Language card ─────────────────────────────────────────────────────────────

@Composable
private fun LanguageCard(
    language: SupportedLanguage,
    isSelected: Boolean,
    alpha: Float = 1f,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) Color(0xFF1A3A0D).copy(alpha = 0.7f)
                else Color(0xFF000000).copy(alpha = 0.40f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Color(0xFF4CAF50) else Color(0xFFFFFFFF).copy(alpha = 0.12f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.VolumeUp,
            contentDescription = null,
            tint = if (isSelected) SdkGreen500 else Color(0xFF6B7C69),
            modifier = Modifier.size(18.dp)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        ) {
            val displayName = language.displayName.ifBlank { language.name }
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 13.sp
            )
            if (language.name != displayName) {
                Text(
                    text = language.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
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
        if (!isSelected) Spacer(Modifier.width(20.dp))
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Maps raw API/network errors to friendly messages. */
private fun friendlyError(raw: String?): String = when {
    raw.isNullOrBlank() -> "Unable to load languages.\nPlease check your connection and try again."
    raw.contains("400") || raw.contains("bad request", ignoreCase = true) ->
        "Couldn't fetch languages for your region.\nPlease try again."
    raw.contains("401") || raw.contains("403") || raw.contains("unauthorized", ignoreCase = true) ->
        "Session expired. Please restart the app and try again."
    raw.contains("404") ->
        "Language service is currently unavailable.\nPlease try again later."
    raw.contains("5") && raw.length == 3 ->
        "Server is temporarily unavailable.\nPlease try again in a moment."
    raw.contains("Unable to resolve host", ignoreCase = true) ||
            raw.contains("timeout", ignoreCase = true) ||
            raw.contains("connect", ignoreCase = true) ->
        "No internet connection.\nPlease check your network and try again."
    else -> "Something went wrong.\nPlease try again."
}

// LazyGrid shimmer helper (no key)
private fun androidx.compose.foundation.lazy.grid.LazyGridScope.items(
    count: Int,
    itemContent: @Composable androidx.compose.foundation.lazy.grid.LazyGridItemScope.(Int) -> Unit
) {
    repeat(count) { index -> item { itemContent(index) } }
}
