package com.example.remarket.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Datos de ejemplo para navegación básica
private val sampleProducts = listOf(
    SampleProduct("1", "Samsung S24 Ultra"),
    SampleProduct("2", "Apple iPhone 13 Pro")
)

data class SampleProduct(
    val id: String,
    val title: String
)

/**
 * Pantalla principal básica con navegación a detalle de producto, creación y logout.
 */
@Composable
fun HomeScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenido a ReMarket",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Lista de productos de ejemplo
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sampleProducts) { product ->
                    Button(
                        onClick = { onNavigateToProductDetail(product.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Text(text = product.title)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navegar a crear producto
            Button(
                onClick = onNavigateToCreateProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(text = "Crear nuevo producto")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Logout
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
