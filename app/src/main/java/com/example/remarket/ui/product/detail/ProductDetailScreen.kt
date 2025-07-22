// ui/product/detail/ProductDetailScreen.kt
package com.example.remarket.ui.product.detail

import ReportDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.remarket.data.model.Chat
import com.example.remarket.data.model.Product
import com.example.remarket.util.Resource
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onBuyProduct: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToChat: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val chatState by viewModel.chatState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val auth = FirebaseAuth.getInstance()

    // Estado local para mostrar el diálogo de confirmación de venta
    var showSaleDialog by remember { mutableStateOf(false) }

    // Mostrar Snackbars
    LaunchedEffect(uiState.saleConfirmMsg) {
        uiState.saleConfirmMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaleMsg()
        }
    }
    LaunchedEffect(uiState.reportMessage) {
        uiState.reportMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearReportMessage()
        }
    }

    // Cargar producto al iniciar
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    // Navegar al chat si chatState es éxito
    LaunchedEffect(chatState) {
        if (chatState is Resource.Success) {
            val chatId = (chatState as Resource.Success<Chat>).data.id
            if (chatId.isNotEmpty()) {
                onNavigateToChat(chatId)
                viewModel.clearChatState()
            }
        }
    }

    // Volver tras eliminación
    LaunchedEffect(uiState.deleteMessage) {
        if (uiState.deleteMessage == "Producto eliminado.") {
            onNavigateBack()
        }
    }

    // Diálogo de confirmación de borrado
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDelete() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
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
                }
            )
        },
        floatingActionButton = {
            // FAB pequeño para “Marcar como vendido”
            if (uiState.isOwner && uiState.product?.status == "reserved") {
                FloatingActionButton(
                    onClick = { showSaleDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Marcar como vendido",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorSection(
                        message = uiState.error!!,
                        onRetry = { viewModel.loadProduct(productId) }
                    )
                }
                else -> {
                    uiState.product?.let { product ->
                        ProductDetailContent(
                            product = product,
                            sellerName = uiState.sellerName,
                            isOwner = uiState.isOwner,
                            onBuyProduct = onBuyProduct,
                            onEditClick = { onNavigateToEdit(product.id) },
                            onDeleteClick = { viewModel.onDeleteClicked() },
                            onContactSeller = { viewModel.onContactSellerClicked(product.id) }
                        )
                    }
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

            // Diálogo de confirmación de venta
            if (showSaleDialog) {
                AlertDialog(
                    onDismissRequest = { showSaleDialog = false },
                    title = { Text("Confirmar Venta") },
                    text = { Text("¿Estás seguro de que quieres marcar este producto como vendido?") },
                    confirmButton = {
                        TextButton(onClick = {
                            viewModel.confirmSale()
                            showSaleDialog = false
                        }) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaleDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ProductDetailContent(
    product: Product,
    sellerName: String?,
    isOwner: Boolean,
    onBuyProduct: (String) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onContactSeller: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)  // deja espacio para la FAB
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            items(product.images) { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen del producto",
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .aspectRatio(1f)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "${product.brand} ${product.model} ${product.storage}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "S/ ${"%.2f".format(product.price)}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp))

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            DetailRow(Icons.Default.Info, "Descripción", product.description)
            DetailRow(Icons.Default.Person, "Vendedor", sellerName ?: "Cargando...")
            DetailRow(Icons.Default.Inventory, "Incluye caja",
                if (product.box.isNotBlank()) "Sí" else "No"
            )
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isOwner) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEditClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Editar")
                    }
                    OutlinedButton(onClick = onDeleteClick, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onContactSeller,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "Contactar")
                    Spacer(Modifier.width(8.dp))
                    Text("Contactar Vendedor")
                }
                Button(
                    onClick = { onBuyProduct(product.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Comprar")
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.bodyLarge)
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
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Reintentar")
        }
    }
}
