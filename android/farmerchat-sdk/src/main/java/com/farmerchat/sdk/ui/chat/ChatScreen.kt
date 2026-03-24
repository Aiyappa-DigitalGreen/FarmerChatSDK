package com.farmerchat.sdk.ui.chat

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.farmerchat.sdk.FarmerChatSdk
import com.farmerchat.sdk.ui.chat.component.ChatInputBar
import com.farmerchat.sdk.ui.chat.component.ChatThreadContent
import com.farmerchat.sdk.ui.chat.component.ImageSourcePickerSheet
import com.farmerchat.sdk.ui.chat.component.ShimmerBlock
import com.farmerchat.sdk.ui.chat.udf.ChatAction
import com.farmerchat.sdk.ui.components.VoiceInputSheet
import com.farmerchat.sdk.ui.components.rememberPhotoInputLaunchers
import com.farmerchat.sdk.ui.theme.LocalSdkExtendedColors
import com.farmerchat.sdk.utils.AudioPlayback
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    conversationId: String?,
    onNavigateToHistory: () -> Unit,
    onNavigateUp: () -> Unit,
    viewModel: ChatViewModel = koinViewModel(parameters = { parametersOf(conversationId) })
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val audioPlayback = remember { AudioPlayback() }
    DisposableEffect(Unit) { onDispose { audioPlayback.release() } }

    LaunchedEffect(state.audioPlaybackUrl) {
        val url = state.audioPlaybackUrl
        if (!url.isNullOrEmpty()) {
            audioPlayback.play(
                url = url,
                onCompletion = { viewModel.dispatch(ChatAction.SetAudioPlaying(false)) },
                onError = { viewModel.dispatch(ChatAction.SetAudioPlaying(false)) }
            )
            viewModel.dispatch(ChatAction.SetAudioPlaying(true))
            viewModel.dispatch(ChatAction.ClearAudioPlaybackUrl)
        }
    }

    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showVoiceSheet by remember { mutableStateOf(false) }
    var showImageSourceSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var pendingMicAction by remember { mutableStateOf(false) }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingMicAction) { pendingMicAction = false; showVoiceSheet = true }
        pendingMicAction = false
    }

    var pendingCameraAction by remember { mutableStateOf(false) }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingCameraAction) { pendingCameraAction = false; showImageSourceSheet = true }
        pendingCameraAction = false
    }

    val photoLaunchers = rememberPhotoInputLaunchers { uri ->
        selectedImageUri = uri
        showImageSourceSheet = false
    }

    fun hasMicPermission() = context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun hasCameraPermission() = context.checkSelfPermission(Manifest.permission.CAMERA) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

    fun onMicClicked() {
        if (hasMicPermission()) showVoiceSheet = true
        else { pendingMicAction = true; micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO) }
    }

    fun onCameraClicked() {
        if (hasCameraPermission()) showImageSourceSheet = true
        else { pendingCameraAction = true; cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
    }

    val config = runCatching { FarmerChatSdk.config }.getOrNull()
    val extColors = LocalSdkExtendedColors.current
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        containerColor = extColors.chatBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Avatar circle with gradient
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = Color.White.copy(alpha = 0.22f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = config?.aiAvatarEmoji ?: "🌱",
                                fontSize = 18.sp
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(7.dp)
                            ) {
                                Text(
                                    text = config?.chatTitle ?: "FarmerChat",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = extColors.topBarTitle
                                )
                                // Online indicator — white dot visible on green top bar
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = CircleShape,
                                    color = Color(0xFF69F0AE)
                                ) {}
                            }
                            Text(
                                text = config?.chatSubtitle ?: "AI Farm Assistant",
                                style = MaterialTheme.typography.labelSmall,
                                color = extColors.topBarTitle.copy(alpha = 0.72f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = extColors.topBarTitle
                        )
                    }
                },
                actions = {
                    if (config?.showHistoryButton != false) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "Chat history",
                                tint = extColors.topBarTitle
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.drawBehind {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                extColors.topBarBackground,
                                extColors.topBarBackground.copy(alpha = 0.88f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
            )
        },
        bottomBar = {
            if (!showVoiceSheet) {
                ChatInputBar(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onSend = {
                        val text = inputText.trim()
                        val imageUri = selectedImageUri
                        if (text.isBlank() && imageUri == null) return@ChatInputBar
                        when {
                            imageUri != null -> {
                                viewModel.dispatch(ChatAction.SendQuestionWithImage(question = text, imageUri = imageUri))
                                selectedImageUri = null
                            }
                            state.messages.isEmpty() -> viewModel.dispatch(ChatAction.InitializeWithQuestion(question = text))
                            else -> viewModel.dispatch(ChatAction.SendFollowUpQuestion(question = text))
                        }
                        inputText = ""
                    },
                    onCameraClick = { onCameraClicked() },
                    onMicClick = { onMicClicked() },
                    selectedImageUri = selectedImageUri,
                    onClearImage = { selectedImageUri = null },
                    isLoading = state.isLoading
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading && state.messages.isEmpty() -> {
                    Column { repeat(3) { ShimmerBlock() } }
                }
                else -> {
                    ChatThreadContent(
                        state = state,
                        listState = listState,
                        onFollowUpSelected = { question, questionId ->
                            viewModel.dispatch(ChatAction.SendFollowUpQuestion(question = question, followUpQuestionId = questionId))
                        },
                        onListenClick = {
                            if (state.isAudioPlaying) {
                                audioPlayback.stop()
                                viewModel.dispatch(ChatAction.SetAudioPlaying(false))
                            } else {
                                viewModel.dispatch(ChatAction.SynthesiseAudio)
                            }
                        },
                        onRetry = { viewModel.dispatch(ChatAction.RetryLastRequest) },
                        contentPadding = PaddingValues(vertical = 8.dp),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showVoiceSheet) {
        VoiceInputSheet(
            onDismiss = { showVoiceSheet = false },
            onAudioCaptured = { base64, format, _ ->
                showVoiceSheet = false
                viewModel.dispatch(ChatAction.TranscribeAndSendAudio(audioBase64 = base64, audioFormat = format, audioUri = null))
            }
        )
    }

    if (showImageSourceSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showImageSourceSheet = false },
            sheetState = sheetState
        ) {
            ImageSourcePickerSheet(
                onCameraSelected = { photoLaunchers.launchCamera() },
                onGallerySelected = { photoLaunchers.launchGallery() },
                onDismiss = { showImageSourceSheet = false }
            )
        }
    }
}
