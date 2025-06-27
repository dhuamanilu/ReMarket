// ui/product/detail/ProductDetailScreen.kt
package com.example.remarket.ui.product.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.remarket.data.model.Product


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onBuyProduct: (String) -> Unit,
    viewModel: ProductDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoaded by viewModel.isProductLoaded.collectAsState()

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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF))
        )

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6C63FF))
                }
            }
            uiState.error != null -> {
                ErrorSection(message = uiState.error!!, onRetry = { viewModel.clearError() })
            }
            isLoaded && uiState.product != null -> {
                ProductDetailContent(
                    product = uiState.product!!,
                    sellerName = uiState.sellerName,
                    isFavorite = uiState.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite() },
                    onBuyProduct = onBuyProduct
                )
            }
        }

        if (uiState.showReportDialog) {
            ReportDialog(
                isReporting = uiState.isReporting,
                onDismiss = { viewModel.hideReportDialog() },
                onReport = { viewModel.reportProduct(it) }
            )
        }
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
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(text = message, color = Color.Red, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))) {
            Text("Reintentar")
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onBuyProduct: (String) -> Unit,
    sellerName: String? = null
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Carrusel de imágenes
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(product.images.size) { idx ->
                AsyncImage(
                    model = product.images[idx],
                    contentDescription = "Imagen ${idx + 1}",
                    modifier = Modifier
                        .size(width = 200.dp, height = 200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Precio, marca y modelo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("S/${product.price.toInt()}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("${product.brand} ${product.model}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Detalles adicionales
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            DataRow(label = "Almacenamiento", value = product.storage)
            DataRow(label = "Descripción", value = product.description)
            DataRow(label = "Incluye caja", value = if (product.box == "yes") "Sí" else "No")
            DataRow(label = "Vendedor", value = sellerName ?: "Cargando...")
            DataRow(label = "Creado", value = product.createdAt)
        }

        Spacer(Modifier.height(16.dp))


        Spacer(Modifier.height(24.dp))

        // Comprar
        Button(
            onClick = { onBuyProduct(product.id) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
        ) {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("Comprar", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text = text, fontSize = 12.sp)
    }
}

@Composable
private fun DataRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(value, fontSize = 14.sp, color = Color(0xFF555555))
    }
}
