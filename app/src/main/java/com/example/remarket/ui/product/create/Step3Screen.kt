package com.example.remarket.ui.product.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
import com.example.remarket.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Galería de imágenes
                    Text(
                        text = "Fotos del producto",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                                        .background(
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar imagen",
                                        tint = MaterialTheme.colorScheme.primary
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

                    // Foto de la caja
                    Text(
                        text = "Foto de la caja (opcional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
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
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar caja",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Foto de la factura
                    Text(
                        text = "Foto de la factura (opcional)",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
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
                                    .background(
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Eliminar factura",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Estado y errores
                    when (state) {
                        is Resource.Error -> {
                            Text(
                                text = (state as Resource.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        is Resource.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(bottom = 8.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        else -> {}
                    }

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Atrás",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { viewModel.submit(onSuccess = onSubmit) },
                            enabled = state !is Resource.Loading,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "Enviar a Revisión",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
