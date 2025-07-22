// File: app/src/main/java/com/example/remarket/ui/purchase/PurchaseScreen.kt
package com.example.remarket.ui.purchase

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(
    productId: String,
    onBack: () -> Unit,
    onPurchaseComplete: () -> Unit,
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val ui by viewModel.ui.collectAsState()

    LaunchedEffect(ui.success) {
        if (ui.success) onPurchaseComplete()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmar compra") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padd ->
        Box(Modifier.padding(padd).fillMaxSize(), Alignment.Center) {
            when {
                ui.isLoading -> CircularProgressIndicator()
                ui.error != null -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(ui.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { viewModel.purchase(productId) }) { Text("Reintentar") }
                    }
                }
                else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("¿Quieres reservar este producto?")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.purchase(productId) }) { Text("Comprar") }
                }
            }
        }
    }
}
