// File: app/src/main/java/com/example/remarket/ui/common/ImagePickerItem.kt
package com.example.remarket.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ImagePickerItem(
    imageUri: String?,
    size: Dp,
    onPick: (String) -> Unit
) {
    val ctx = LocalContext.current

    // Launcher para cámara (preview bitmap)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let { bitmap ->
            // Convertir bitmap a Uri temporal
            val uri = saveTempBitmap(ctx, bitmap)
            onPick(uri.toString())
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPick(it.toString()) }
    }

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (!imageUri.isNullOrBlank()) {
            val bmp = decodeBitmap(imageUri, ctx)
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(size)
                    .clickable { galleryLauncher.launch("image/*") }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Seleccionar",
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Cámara",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { cameraLauncher.launch() }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Galería",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { galleryLauncher.launch("image/*") }
                    )
                }
            }
        }
    }
}

// Helpers
fun decodeBitmap(uriString: String, context: Context) =
    android.graphics.BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(Uri.parse(uriString))
    )

fun saveTempBitmap(context: Context, bitmap: Bitmap): Uri {
    // Guarda en cache dir y retorna Uri
    val file = java.io.File(context.cacheDir, "temp_image.jpg")
    java.io.FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    return androidx.core.content.FileProvider.getUriForFile(
        context,
        context.packageName + ".provider",
        file
    )
}