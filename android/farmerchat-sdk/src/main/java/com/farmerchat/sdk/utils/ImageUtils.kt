package com.farmerchat.sdk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Base64

internal object ImageUtils {

    /**
     * Loads a Bitmap from a URI, downsampling if necessary.
     */
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Converts an image URI to a base64-encoded JPEG string, downscaling to maxDimension if needed.
     */
    fun getBase64FromUri(context: Context, uri: Uri, maxDimension: Int = 1024): String {
        val bitmap = getBitmapFromUri(context, uri)
            ?: throw IllegalArgumentException("Could not decode image from URI: $uri")

        val rotatedBitmap = rotateImageIfRequired(context, bitmap, uri)
        val scaledBitmap = scaleBitmap(rotatedBitmap, maxDimension)

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Extracts GPS coordinates from EXIF data if available.
     * Returns Pair<latitude, longitude> as decimal degree strings.
     */
    fun getLocationFromExif(context: Context, uri: Uri): Pair<String, String>? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val latLong = FloatArray(2)
                if (exif.getLatLong(latLong)) {
                    Pair(latLong[0].toString(), latLong[1].toString())
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generates a unique image file name based on timestamp.
     */
    fun generateUniqueImageName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return "farmerchat_img_$timestamp.jpg"
    }

    private fun scaleBitmap(bitmap: Bitmap, maxDimension: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDimension && height <= maxDimension) return bitmap

        val scale = maxDimension.toFloat() / maxOf(width, height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, uri: Uri): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
                val rotation = when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
                if (rotation != 0f) {
                    val matrix = Matrix().apply { postRotate(rotation) }
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                } else bitmap
            } ?: bitmap
        } catch (e: Exception) {
            bitmap
        }
    }
}
