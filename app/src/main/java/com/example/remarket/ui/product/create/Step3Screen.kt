// File: app/src/main/java/com/example/remarket/ui/product/create/Step3Screen.kt
package com.example.remarket.ui.product.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
import com.example.remarket.util.Resource

/**
 * Tercera pantalla: fotos de producto, caja y factura, y botón de envío.
 */
@Composable
fun Step3Screen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    val images by viewModel.images.collectAsState()
    val boxImage by viewModel.boxImageUrl.collectAsState()
    val invoiceImage by viewModel.invoiceUrl.collectAsState()
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Fotos del producto",
            style = MaterialTheme.typography.titleLarge
        )

        // Galería de imágenes del producto
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(images) { _, uri ->
                ImagePickerItem(
                    imageUri = uri,
                    size = 100.dp,
                    onPick = {} // no permite reemplazar
                )
            }
            item {
                ImagePickerItem(
                    imageUri = null,
                    size = 100.dp,
                    onPick = { viewModel.addImage(it) }
                )
            }
        }

        Text(
            text = "Foto de la caja",
            style = MaterialTheme.typography.titleMedium
        )
        ImagePickerItem(
            imageUri = boxImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setBoxImage(it) }
        )

        Text(
            text = "Foto de la factura",
            style = MaterialTheme.typography.titleMedium
        )
        ImagePickerItem(
            imageUri = invoiceImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setInvoiceImage(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botones Atrás y Enviar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Atrás")
            }
            val context = LocalContext.current

            Button(
                onClick = { viewModel.submit(context, onSuccess = onSubmit) },
                enabled = state !is Resource.Loading
            ) {
                Text("Enviar")
            }
        }

        // Estado de envío
        when (state) {
            is Resource.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is Resource.Error -> Text(
                text = (state as Resource.Error).message,
                color = MaterialTheme.colorScheme.error
            )
            is Resource.Success -> {/* opcional: mensaje de éxito */}
            else -> {}
        }
    }
}