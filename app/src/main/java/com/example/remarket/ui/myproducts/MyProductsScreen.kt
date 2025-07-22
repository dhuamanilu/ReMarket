package com.example.remarket.ui.myproducts

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage

import com.example.remarket.data.model.Product

@Composable
fun MyProductsScreen(
    uiState: MyProductsViewModel.UiState,
    paddingValues: PaddingValues,
    onNavigateToProductDetail: (String) -> Unit
) {
    when {
        uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(uiState.error ?: "Error")
        }
        else -> LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ▸ Sección de productos comprados
            if (uiState.bought.isNotEmpty()) {
                item {
                    Text(
                        text = "Comprados",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }
                items(uiState.bought) { p ->
                    ProductRow(p, onNavigateToProductDetail)
                }
                item { Spacer(Modifier.height(24.dp)) }
            }

            // ▸ Sección de productos en venta
            if (uiState.products.isNotEmpty()) {
                item {
                    Text(
                        text = "En venta",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                    )
                }
                items(uiState.products) { p ->
                    ProductRow(p, onNavigateToProductDetail)
                }
            }

            // ▸ Mensaje si no hay nada
            if (uiState.bought.isEmpty() && uiState.products.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No tienes productos para mostrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(p: Product,onNavigateToProductDetail: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onNavigateToProductDetail(p.id) },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = p.images.firstOrNull(),
                contentDescription = "${p.brand} ${p.model}",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Columna de información del producto
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Marca y modelo
                Text(
                    text = "${p.brand} ${p.model}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Estado con chip de color
                StatusChip(status = p.status)
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (statusText, backgroundColor, textColor) = when (status.uppercase()) {
        "APPROVED" -> Triple(
            "Aprobado",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        "PENDING" -> Triple(
            "Pendiente",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        "REJECT" -> Triple(
            "Rechazado",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        "RESERVED" -> Triple(
            "Reservado",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        "SOLD" -> Triple(
            "Comprado",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Triple(
            status,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
