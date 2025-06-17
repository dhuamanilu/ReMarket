// File: app/src/main/java/com/example/remarket/ui/common/ImagePickerItem.kt
package com.example.remarket.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
/** Decodifica un bitmap a partir de una Uri en String **/
fun decodeBitmap(uriString: String, context: android.content.Context) =
    android.graphics.BitmapFactory.decodeStream(
        context.contentResolver.openInputStream(android.net.Uri.parse(uriString))
    )
@Composable
fun ImagePickerItem(
    imageUri: String?,
    size: Dp,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onPick(it.toString()) }
    }
    Box(
        modifier = modifier
            .size(size)
            .clickable { launcher.launch("image/*") },
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
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar imagen",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(size.times(0.5f))
            )
        }
    }
}