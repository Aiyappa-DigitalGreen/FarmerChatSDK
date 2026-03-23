package com.farmerchat.sdk.utils

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

private const val TAG = "SdkAudioPlayback"

internal class AudioPlayback {

    private var mediaPlayer: MediaPlayer? = null

    /**
     * Play audio from the given URL.
     * @param url URL of the audio stream.
     * @param onCompletion Called when playback completes.
     * @param onError Called if playback encounters an error.
     */
    fun play(
        url: String,
        onCompletion: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        stop()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(url)
                setOnCompletionListener {
                    onCompletion()
                    release()
                    mediaPlayer = null
                }
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what extra=$extra")
                    onError("Playback error ($what, $extra)")
                    release()
                    mediaPlayer = null
                    true
                }
                prepareAsync()
                setOnPreparedListener { start() }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play audio", e)
                onError(e.localizedMessage ?: "Failed to play audio")
                release()
                mediaPlayer = null
            }
        }
    }

    /**
     * Stop current playback.
     */
    fun stop() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping playback", e)
        } finally {
            mediaPlayer = null
        }
    }

    /**
     * Returns true if audio is currently playing.
     */
    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    /**
     * Release all resources.
     */
    fun release() = stop()
}
