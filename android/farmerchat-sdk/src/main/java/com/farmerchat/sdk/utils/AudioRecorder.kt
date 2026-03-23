package com.farmerchat.sdk.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import android.util.Log
import java.io.File

private const val TAG = "SdkAudioRecorder"

/**
 * Wraps MediaRecorder for simple voice recording.
 *
 * Usage:
 * ```kotlin
 * val recorder = AudioRecorder(context)
 * recorder.startRecording(file)
 * val base64 = recorder.stopRecording()
 * ```
 */
internal class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    /**
     * Start recording audio to the given file.
     * Uses OGG/OPUS on Android 10+ (API 29+), falls back to AAC in MPEG-4.
     */
    fun startRecording(file: File) {
        outputFile = file
        recorder = createMediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setOutputFormat(MediaRecorder.OutputFormat.OGG)
                setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
            } else {
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            }
            setAudioSamplingRate(16_000)
            setAudioEncodingBitRate(32_000)
            setOutputFile(file.absolutePath)
            try {
                prepare()
                start()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start recording", e)
                release()
                this@AudioRecorder.recorder = null
                throw e
            }
        }
    }

    /**
     * Stop recording and return the audio data as a base64-encoded string.
     * The caller is responsible for deleting the temp file when done.
     */
    fun stopRecording(): String {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recorder", e)
        } finally {
            recorder = null
        }
        val file = outputFile ?: throw IllegalStateException("No output file — was startRecording() called?")
        val bytes = file.readBytes()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Cancel ongoing recording and delete the temp file.
     */
    fun cancel() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling recorder", e)
        } finally {
            recorder = null
            outputFile?.delete()
            outputFile = null
        }
    }

    /**
     * Release recorder resources without saving.
     */
    fun release() {
        recorder?.release()
        recorder = null
    }

    /**
     * Returns the audio encoding format string expected by the transcription API.
     */
    fun getAudioFormat(): String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        "OGG_OPUS"
    } else {
        "AAC"
    }

    @Suppress("DEPRECATION")
    private fun createMediaRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
}
