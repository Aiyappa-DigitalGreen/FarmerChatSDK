package com.farmerchat.sdk.ui.history

import androidx.compose.animation.core.Animatable
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.domain.model.history.ConversationListItem
import com.farmerchat.sdk.ui.chat.component.ShimmerBlock
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors
import com.farmerchat.sdk.ui.theme.SdkTextMuted
import com.farmerchat.sdk.ui.theme.SdkTextSecondary
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// Design-exact colors
private val HistoryBackground = Color(0xFF0D1A0B)
private val HistoryCardBg     = Color(0xFF172213)
private val HistoryGroupLabel = Color(0xFF4A5E48)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HistoryScreen(
    onNavigateUp: () -> Unit,
    onConversationSelected: (String) -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val extColors = LocalSdkExtendedColors.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                if (totalItems > 0 && lastVisible >= totalItems - 3) {
                    viewModel.loadNextPage()
                }
            }
    }

    Scaffold(
        containerColor = HistoryBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Chat History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Your farming conversations",
                            style = MaterialTheme.typography.bodySmall,
                            color = SdkTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // "+" green circle 36dp in actions
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50))
                            .clickable { onNavigateUp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "New chat",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HistoryBackground
                )
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            when {
                state.isLoading && state.conversations.isEmpty() -> {
                    Column(modifier = Modifier.fillMaxSize()) { repeat(8) { ShimmerBlock() } }
                }
                state.errorMessage != null && state.conversations.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.errorMessage ?: "Failed to load conversations",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                !state.isLoading && !state.isRefreshing && state.conversations.isEmpty() -> {
                    EmptyHistoryContent(modifier = Modifier.fillMaxSize())
                }
                else -> {
                    val filteredGrouped = if (searchQuery.isBlank()) {
                        state.groupedConversations
                    } else {
                        state.groupedConversations.mapValues { (_, items) ->
                            items.filter {
                                (it.conversation_title ?: "").contains(searchQuery, ignoreCase = true)
                            }
                        }.filter { it.value.isNotEmpty() }
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item {
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                        filteredGrouped.forEach { (group, items) ->
                            item(key = "header_$group") {
                                GroupHeader(
                                    label = group,
                                    modifier = Modifier.animateItem(tween(300))
                                )
                            }
                            itemsIndexed(items = items, key = { _, item -> item.conversation_id }) { index, item ->
                                ConversationCard(
                                    item = item,
                                    index = index,
                                    onClick = {
                                        FarmerChatSdk.config.analyticsListener
                                            ?.onHistoryConversationSelected(item.conversation_id)
                                        onConversationSelected(item.conversation_id)
                                    },
                                    modifier = Modifier.animateItem(tween(300))
                                )
                            }
                        }
                        if (state.isLoading && state.conversations.isNotEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    // bg=Color(0xFF172213), corners=14dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HistoryCardBg)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search conversations...", color = SdkTextMuted, fontSize = 14.sp) },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = "Search",
                    tint = SdkTextMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
        )
    }
}

@Composable
private fun GroupHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = HistoryGroupLabel,
        letterSpacing = 1.5.sp,
        fontSize = 10.sp,
        modifier = modifier.padding(start = 20.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
    )
}

private data class ConvoIcon(val emoji: String, val color: Color)

private fun iconForConversation(item: ConversationListItem): ConvoIcon {
    val t = (item.conversation_title ?: "").lowercase()
    return when {
        "tomato" in t || "vegetable" in t         -> ConvoIcon("🍅", Color(0xFFE53935))
        "weather" in t || "rain" in t || "monsoon" in t -> ConvoIcon("🌧️", Color(0xFF1565C0))
        "soil" in t || "npk" in t                 -> ConvoIcon("🌱", Color(0xFF558B2F))
        "irrigation" in t || "water" in t         -> ConvoIcon("💧", Color(0xFF0288D1))
        "fertilizer" in t || "nutrient" in t      -> ConvoIcon("🌻", Color(0xFFF9A825))
        "pest" in t || "insect" in t              -> ConvoIcon("🐛", Color(0xFF6D4C41))
        "wheat" in t || "rice" in t || "crop" in t -> ConvoIcon("🌾", Color(0xFF8D6E63))
        "disease" in t || "virus" in t            -> ConvoIcon("⚠️", Color(0xFFE65100))
        item.message_type?.lowercase() == "image" -> ConvoIcon("📸", Color(0xFF6A1B9A))
        item.message_type?.lowercase() == "audio" -> ConvoIcon("🎤", Color(0xFF00838F))
        else -> ConvoIcon("💬", Color(0xFF2E7D32))
    }
}

private fun topicTag(item: ConversationListItem): Pair<String, Color>? {
    val t = (item.conversation_title ?: "").lowercase()
    return when {
        "tomato" in t || "vegetable" in t         -> "🍅 Tomato" to Color(0xFFE53935)
        "weather" in t || "rain" in t || "monsoon" in t -> "🌧 Weather" to Color(0xFF1565C0)
        "soil" in t || "npk" in t                 -> "🌱 Soil" to Color(0xFF558B2F)
        "irrigation" in t || "water" in t         -> "💧 Irrigation" to Color(0xFF0288D1)
        "fertilizer" in t || "nutrient" in t      -> "🌻 Fertilizer" to Color(0xFFF9A825)
        "pest" in t                               -> "🐛 Pest" to Color(0xFF6D4C41)
        "wheat" in t || "rice" in t               -> "🌾 Crop" to Color(0xFF8D6E63)
        "disease" in t || "virus" in t            -> "⚠️ Disease" to Color(0xFFE65100)
        item.message_type?.lowercase() == "image" -> "📸 Analysis" to Color(0xFF6A1B9A)
        else -> null
    }
}

@Composable
private fun ConversationCard(
    item: ConversationListItem,
    index: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = iconForConversation(item)
    val tag  = topicTag(item)

    // Staggered entrance: slide up + fade in
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(28f) }
    LaunchedEffect(item.conversation_id) {
        kotlinx.coroutines.delay(index * 40L)
        kotlinx.coroutines.coroutineScope {
            launch { alpha.animateTo(1f, tween(300)) }
            launch { offsetY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha.value)
            .offset { IntOffset(0, offsetY.value.toInt()) }
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(HistoryCardBg)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: 44dp circle bg=icon_color.copy(alpha=0.18f) with emoji 20sp
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(icon.color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon.emoji, fontSize = 20.sp)
        }

        Spacer(Modifier.width(12.dp))

        // Center column: title (1 line ellipsis) + Row(time + tag chip)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.conversation_title ?: "Untitled conversation",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.White,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatDate(item.created_on),
                    style = MaterialTheme.typography.bodySmall,
                    color = SdkTextSecondary,
                    fontSize = 11.sp
                )
                if (tag != null) {
                    // Tag chip: bg=tag_color.copy(alpha=0.15f), padding h=6dp v=2dp, corner=8dp
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(tag.second.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tag.first,
                            fontSize = 10.sp,
                            color = tag.second,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Right: "›" 20sp Color(0xFF4A5E48)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "›",
            color = HistoryGroupLabel,
            fontSize = 20.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) { Text(text = "💬", fontSize = 40.sp) }
        Spacer(Modifier.height(20.dp))
        Text(
            "No conversations yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Your past conversations will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = SdkTextSecondary
        )
    }
}

private val inputFormatter = DateTimeFormatter.ofPattern(
    "yyyy-MM-dd'T'HH:mm:ss[.SSSSSS][.SSS][.SS][.S]", Locale.getDefault()
)
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
private val dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())
private val yearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())

private fun formatDate(dateString: String): String {
    return try {
        val dt = LocalDateTime.parse(dateString.trim(), inputFormatter)
        val today = LocalDate.now()
        val date  = dt.toLocalDate()
        when {
            date == today              -> dt.format(timeFormatter)
            date == today.minusDays(1) -> "Yesterday · ${dt.format(timeFormatter)}"
            date.year == today.year    -> dt.format(dateFormatter)
            else                       -> dt.format(yearFormatter)
        }
    } catch (e: Exception) {
        dateString.replace("T", " ").substringBefore(".").trim()
    }
}
