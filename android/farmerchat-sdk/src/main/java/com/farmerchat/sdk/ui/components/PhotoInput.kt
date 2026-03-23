package com.farmerchat.sdk.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

/**
 * Remembers launchers for camera capture and gallery pick.
 * Returns a pair: (launchCamera, launchGallery).
 * On result, calls onImageSelected with the resulting URI.
 */
@Composable
internal fun rememberPhotoInputLaunchers(
    onImageSelected: (Uri) -> Unit
): PhotoInputLaunchers {
    val context = LocalContext.current
    val tempUri = remember { mutableListOf<Uri>() }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri.lastOrNull()?.let { onImageSelected(it) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImageSelected(it) }
    }

    return PhotoInputLaunchers(
        launchCamera = {
            val uri = createTempImageUri(context)
            tempUri.clear()
            tempUri.add(uri)
            cameraLauncher.launch(uri)
        },
        launchGallery = {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    )
}

internal data class PhotoInputLaunchers(
    val launchCamera: () -> Unit,
    val launchGallery: () -> Unit
)

private fun createTempImageUri(context: Context): Uri {
    val cacheDir = File(context.cacheDir, "farmerchat").apply { mkdirs() }
    val imageFile = File.createTempFile("fc_img_", ".jpg", cacheDir)
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.farmerchat.fileprovider",
        imageFile
    )
}
