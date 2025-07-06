// File: app/src/main/java/com/example/remarket/ui/product/create/Step3Screen.kt
package com.example.remarket.ui.product.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items // <-- ¡IMPORTACIÓN CLAVE!

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
import com.example.remarket.util.Resource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close // Añade para el ícono 'X'
import androidx.compose.ui.graphics.Color
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
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Fotos del producto",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )

        // --- GALERÍA DE IMÁGENES PRINCIPALES CORREGIDA ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Se usa la función 'items' que toma una lista.
            // La variable 'uri' será correctamente un String.
            items(items = images, key = { it }) { uri ->
                Box(contentAlignment = Alignment.TopEnd) {
                    ImagePickerItem(
                        imageUri = uri,
                        size = 100.dp,
                        onPick = {}
                    )
                    IconButton(
                        onClick = { viewModel.removeImage(uri) },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(2.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Eliminar imagen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
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
        Box(contentAlignment = Alignment.TopEnd) {
            ImagePickerItem(
                imageUri = boxImage.takeIf { it.isNotBlank() },
                size = 100.dp,
                onPick = { viewModel.setBoxImage(it) }
            )
            if (boxImage.isNotBlank()) {
                IconButton(
                    onClick = { viewModel.clearBoxImage() },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close, "Eliminar", tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Foto de la factura (opcional)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        Box(contentAlignment = Alignment.TopEnd) {
            ImagePickerItem(
                imageUri = invoiceImage.takeIf { it.isNotBlank() },
                size = 100.dp,
                onPick = { viewModel.setInvoiceImage(it) }
            )
            if (invoiceImage.isNotBlank()) {
                IconButton(
                    onClick = { viewModel.clearInvoiceImage() },
                    modifier = Modifier
                        .size(24.dp)
                        .padding(2.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close, "Eliminar", tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

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
                onClick = { viewModel.submit(onSuccess = onSubmit) },
                enabled = state !is Resource.Loading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Enviar a Revisión")
            }
        }
    }
}