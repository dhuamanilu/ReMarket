// ui/product/detail/ProductDetailScreen.kt
package com.example.remarket.ui.product.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onBuyProduct: (String) -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isProductLoaded by viewModel.isProductLoaded.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Detalle del Producto") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { viewModel.showReportDialog() }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF6C63FF)
            )
        )

        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6C63FF))
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Error",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.clearError() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        )
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            isProductLoaded && uiState.product != null -> {
                ProductDetailContent(
                    product = uiState.product!!,
                    isFavorite = uiState.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onBuyProduct = onBuyProduct
                )
            }
        }

        // Diálogo para reportar
        if (uiState.showReportDialog) {
            ReportDialog(
                isReporting = uiState.isReporting,
                onDismiss = { viewModel.hideReportDialog() },
                onReport = { reason -> viewModel.reportProduct(reason) }
            )
        }

        // Diálogo de éxito tras reportar
        if (uiState.reportSuccess) {
            AlertDialog(
                onDismissRequest = { viewModel.hideReportDialog() },
                title = { Text("Reporte Enviado") },
                text = { Text("Tu reporte ha sido enviado exitosamente.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.hideReportDialog() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: com.example.remarket.data.model.Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBuyProduct: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "${product.brand} ${product.model}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                // Badge de almacenamiento
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.7f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = product.storage,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón de favorito (solo cambia estado, pero no muestra lista de favoritos real)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFF6C63FF) else Color.Gray
                    )
                }
            }
        }

        // Tarjeta con información básica del producto
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Precio
                Text(
                    text = "S/${product.price.toInt()}",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2E2E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Marca y Modelo
                Text(
                    text = "${product.brand} ${product.model}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E2E2E)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón de Comprar
        Button(
            onClick = { onBuyProduct(product.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C63FF)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Comprar",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comprar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ReportDialog(
    isReporting: Boolean,
    onDismiss: () -> Unit,
    onReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("") }
    val reasons = listOf(
        "Contenido inapropiado",
        "Producto falso",
        "Precio sospechoso",
        "Descripción engañosa",
        "Otro"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Producto") },
        text = {
            Column {
                Text("Selecciona la razón del reporte:")
                Spacer(modifier = Modifier.height(8.dp))
                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Text(
                            text = reason,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isReporting) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                TextButton(
                    onClick = { onReport(selectedReason) },
                    enabled = selectedReason.isNotEmpty()
                ) {
                    Text("Reportar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isReporting
            ) {
                Text("Cancelar")
            }
        }
    )
}
