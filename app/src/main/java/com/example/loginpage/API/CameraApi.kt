package com.example.loginpage.API

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

/** Simple controller exposing camera (preview) + gallery pick for profile image */
@Stable
class ProfileImageController internal constructor(
    private val _image: MutableState<ImageBitmap?>,
    val captureFromCamera: () -> Unit,
    val pickFromGallery: () -> Unit
) {
    val image: ImageBitmap? get() = _image.value
    // Added: allow clearing the selected image programmatically
    fun clear() { _image.value = null }
}

@Composable
fun rememberProfileImageController(): ProfileImageController {
    val context = LocalContext.current
    val imageState = remember { mutableStateOf<ImageBitmap?>(null) }

    // Camera launcher (preview bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) imageState.value = bmp.asImageBitmap()
    }

    // Gallery (Photo Picker) launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) imageState.value = bmp.asImageBitmap()
                }
            } catch (_: Exception) {
                // Ignore decode errors
            }
        }
    }

    val capture = remember { { cameraLauncher.launch(null) } }
    val pick = remember {
        {
            galleryLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    return remember { ProfileImageController(imageState, capture, pick) }
}
