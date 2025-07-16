// ui/home/HomeScreen.kt
package com.example.remarket.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.remarket.data.model.Product
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        modifier = Modifier.padding(paddingValues),
        bottomBar = {
            // Contenedor elevado con esquinas redondeadas
            Surface(
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón principal con icono
                    Button(
                        onClick = onNavigateToCreateProduct,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Vender producto"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Vender")
                    }
                    // Botón secundario
                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Cerrar sesión"
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    ) { innerPadding ->
        val swipeState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
        SwipeRefresh(
            state = swipeState,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Encabezado
                item {
                    Text(
                        text = "Bienvenido a ReMarket",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                // Campo de búsqueda
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = onSearchQueryChanged,
                        label = { Text("Buscar marca, modelo…") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Error si no hay caché
                if (uiState.error != null && uiState.filteredProducts.isEmpty()) {
                    item {
                        Text(
                            text = uiState.error,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    }
                }
                // Lista de productos
                items(uiState.filteredProducts) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onNavigateToProductDetail(product.id) }
                    )
                }
                // Placeholder vacío
                if (!uiState.isLoading && uiState.filteredProducts.isEmpty() && uiState.error == null) {
                    item {
                        Text(
                            text = "No se encontraron productos.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                        )
                    }
                }
                // Espacio para no tapar el contenido con el bottomBar
                item {
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            AsyncImage(
                model = product.images.firstOrNull(),
                contentDescription = "${product.brand} ${product.model}",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier
                    .size(96.dp)
                    .padding(8.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                Text(
                    text = "${product.brand} ${product.model}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "S/ ${product.price}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Almacenamiento: ${product.storage} GB",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                if (!product.isSynced) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Pendiente de sincronización",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Sincronizar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Ver detalle",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 12.dp)
            )
        }
    }
}