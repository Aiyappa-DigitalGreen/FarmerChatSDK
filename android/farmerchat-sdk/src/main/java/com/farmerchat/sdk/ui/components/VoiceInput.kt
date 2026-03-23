package com.farmerchat.sdk.ui.components

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.farmerchat.sdk.utils.AudioRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private enum class VoiceInputState {
    Idle, Recording, Processing, Done, PermissionDenied
}

/**
 * Bottom sheet modal for voice input recording.
 * Handles the full lifecycle: permission check → recording → base64 encoding → callback.
 *
 * @param onDismiss Called when the sheet is dismissed without capturing audio.
 * @param onAudioCaptured Called with (base64AudioData, formatString, tempFileUri) when done.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VoiceInputSheet(
    onDismiss: () -> Unit,
    onAudioCaptured: (audioBase64: String, format: String, tempUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var voiceState by remember { mutableStateOf(VoiceInputState.Idle) }
    var recorderRef by remember { mutableStateOf<AudioRecorder?>(null) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check mic permission on entry
    LaunchedEffect(Unit) {
        if (!context.hasAudioPermission()) {
            voiceState = VoiceInputState.PermissionDenied
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            recorderRef?.release()
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            recorderRef?.cancel()
            onDismiss()
        },
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status title
            Text(
                text = when (voiceState) {
                    VoiceInputState.Idle -> "Tap mic to start recording"
                    VoiceInputState.Recording -> "Recording... tap stop when done"
                    VoiceInputState.Processing -> "Processing audio..."
                    VoiceInputState.Done -> "Recording complete"
                    VoiceInputState.PermissionDenied -> "Microphone permission required"
                },
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(4.dp))

            // Waveform
            VoiceClip(
                isAnimating = voiceState == VoiceInputState.Recording,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )

            Spacer(Modifier.height(4.dp))

            // Error text
            errorMessage?.let { err ->
                Text(
                    text = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }

            // Controls
            when (voiceState) {
                VoiceInputState.Idle -> {
                    FilledIconButton(
                        onClick = {
                            errorMessage = null
                            val file = createAudioFile(context)
                            recordingFile = file
                            val rec = AudioRecorder(context)
                            recorderRef = rec
                            try {
                                rec.startRecording(file)
                                voiceState = VoiceInputState.Recording
                            } catch (e: Exception) {
                                recorderRef = null
                                errorMessage = "Could not start recording: ${e.localizedMessage}"
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Start recording",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                VoiceInputState.Recording -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        // Cancel
                        IconButton(
                            onClick = {
                                recorderRef?.cancel()
                                recorderRef = null
                                recordingFile = null
                                voiceState = VoiceInputState.Idle
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Cancel recording",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        // Stop & finalise
                        FilledIconButton(
                            onClick = {
                                voiceState = VoiceInputState.Processing
                                val rec = recorderRef
                                scope.launch {
                                    try {
                                        val base64 = withContext(Dispatchers.IO) {
                                            rec?.stopRecording() ?: ""
                                        }
                                        val format = rec?.getAudioFormat() ?: "AAC"
                                        recorderRef = null
                                        voiceState = VoiceInputState.Done
                                        val fileUri = recordingFile?.let { Uri.fromFile(it) }
                                        onAudioCaptured(base64, format, fileUri)
                                        sheetState.hide()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        errorMessage = "Error saving recording: ${e.localizedMessage}"
                                        voiceState = VoiceInputState.Idle
                                    }
                                }
                            },
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Stop,
                                contentDescription = "Stop recording",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                VoiceInputState.Processing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                VoiceInputState.Done -> {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Done",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                VoiceInputState.PermissionDenied -> {
                    Text(
                        text = "Please grant the Microphone permission in app Settings to use voice input.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

private fun Context.hasAudioPermission(): Boolean =
    checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED

private fun createAudioFile(context: Context): File {
    val dir = File(context.cacheDir, "farmerchat/audio").apply { mkdirs() }
    return File(dir, "recording_${UUID.randomUUID()}.ogg")
}
