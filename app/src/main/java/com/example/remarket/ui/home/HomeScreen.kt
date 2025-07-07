// ui/home/HomeScreen.kt
package com.example.remarket.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.material.icons.Icons // <-- AÑADIR
import androidx.compose.material.icons.filled.CloudOff // <-- AÑADIR
import androidx.compose.material3.Icon
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


/**
 * Pantalla principal básica con navegación a detalle de producto, creación y logout.
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    paddingValues: PaddingValues,
    onSearchQueryChanged: (String) -> Unit,
    onRefresh: () -> Unit, // Callback para iniciar la actualización
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onLogout: () -> Unit
) {
    // Estado para el componente SwipeRefresh, controlado por el ViewModel
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Envolvemos todo en SwipeRefresh
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh, // Se llama cuando el usuario desliza
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Bienvenido a ReMarket",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text("Buscar por marca, modelo...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Si hay un error y no hay productos cacheados, muestra el error.
                if (uiState.error != null && uiState.allProducts.isEmpty()) {
                    Text(
                        text = uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    )
                } else {
                    // La lista siempre se muestra desde el estado, incluso si está vacía.
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.filteredProducts) { product ->
                            // --- BLOQUE MODIFICADO PARA MOSTRAR ESTADO ---
                            Button(
                                onClick = { onNavigateToProductDetail(product.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(text = "${product.brand} ${product.model}")
                                    // Si el producto no está sincronizado, muestra un ícono
                                    if (!product.isSynced) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = "Pendiente de sincronización",
                                            tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Button(
                    onClick = onNavigateToCreateProduct,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(text = "Vender un producto")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(text = "Cerrar sesión")
                }
            }
        }
    }
}
