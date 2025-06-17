// File: app/src/main/java/com/example/remarket/ui/product/create/Step2Screen.kt
package com.example.remarket.ui.product.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
@Composable
fun Step2Screen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    val images = viewModel.images.collectAsState().value
    val boxImage = viewModel.boxImageUrl.collectAsState().value
    val invoiceImage = viewModel.invoiceUrl.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Fotos del producto", style = MaterialTheme.typography.titleLarge)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(images) { _, uriString ->
                ImagePickerItem(
                    imageUri = uriString,
                    size = 100.dp,
                    onPick = {}, // No se reemplaza la imagen en este caso
                    modifier = Modifier
                )
            }
            item {
                ImagePickerItem(
                    imageUri = null,
                    size = 100.dp,
                    onPick = { uri -> viewModel.addImage(uri) }
                )
            }
        }

        Text("Foto de la caja", style = MaterialTheme.typography.titleMedium)
        ImagePickerItem(
            imageUri = boxImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setBoxImage(it) }
        )

        Text("Foto de la factura", style = MaterialTheme.typography.titleMedium)
        ImagePickerItem(
            imageUri = invoiceImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setInvoiceImage(it) }
        )

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack) { Text("Atrás") }
            Button(onClick = onNext) { Text("Siguiente") }
        }
    }
}