// File: app/src/main/java/com/example/remarket/ui/product/create/Step3Screen.kt
package com.example.remarket.ui.product.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
import com.example.remarket.util.Resource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment

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
            .padding(16.dp)
            .verticalScroll(rememberScrollState()), // Añadido para evitar desbordamiento
        horizontalAlignment = Alignment.CenterHorizontally // Centra el contenido
    ) {
        Text(
            text = "Fotos del producto",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        // Galería de imágenes del producto
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Foto de la caja (opcional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        ImagePickerItem(
            imageUri = boxImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setBoxImage(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Foto de la factura (opcional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        ImagePickerItem(
            imageUri = invoiceImage.takeIf { it.isNotBlank() },
            size = 100.dp,
            onPick = { viewModel.setInvoiceImage(it) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Estado de envío
        if (state is Resource.Error) {
            Text(
                text = (state as Resource.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (state is Resource.Loading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 8.dp))
        }

        // Botones Atrás y Enviar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Atrás")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                // --- LLAMADA CORREGIDA ---
                onClick = { viewModel.submit(onSuccess = onSubmit) },
                enabled = state !is Resource.Loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Enviar a Revisión")
            }
        }
    }
}