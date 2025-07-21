package com.example.remarket.ui.admin

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPendingProductsScreen(
    vm: AdminPendingProductsViewModel = hiltViewModel(),
    onProductClick: (String) -> Unit,
    onLogout: () -> Unit,
    navController: NavHostController   // pásalo desde AppNavGraph
) {
    // 🔸  Escucha sólo una vez (LaunchedEffect con key == currentBackStackEntry)
    LaunchedEffect(navController.currentBackStackEntry) {
        navController.currentBackStackEntry
            ?.savedStateHandle
            ?.getLiveData<Boolean>("refreshPending")
            ?.observeForever { shouldRefresh ->
                if (shouldRefresh == true) {
                    vm.load()                       // ← llama otra vez al repo
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.remove<Boolean>("refreshPending")
                }
            }
    }
    val ui = vm.state.collectAsState().value

    Scaffold(topBar = {
        SmallTopAppBar(title = { Text("Pendientes de aprobación") },
            actions = { TextButton(onClick = onLogout) { Text("Salir") } })
    }) { padd ->
        when {
            ui.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(ui.error)
            }
            else -> LazyColumn(
                modifier = Modifier
                    .padding(padd)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(ui.products) { p ->
                    Card(Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("${p.brand} ${p.model}")
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { vm.approve(p.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) { Text("Aceptar") }

                                Button(
                                    onClick = { vm.reject(p.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Rechazar") }

                                TextButton(onClick = {
                                    Log.d("AdminPendingProducts", "Detalle clic en producto con id: ${p.id}")
                                    onProductClick(p.id) }) {
                                    Text("Detalle")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductDetailScreen(
    productId: String,
    onBack: () -> Unit,
    vm: AdminProductDetailViewModel = hiltViewModel()
) {
    val ui = vm.state.collectAsState().value

    LaunchedEffect(productId) {
        vm.loadProduct(productId)
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Detalle de Producto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padd ->
        when {
            ui.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Error: ${ui.error}")
            }
            ui.product != null -> {
                val p = ui.product

                LazyColumn(
                    contentPadding = padd,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Marca: ${p.brand}", style = MaterialTheme.typography.titleMedium)
                        Text("Modelo: ${p.model}")
                        Text("Precio: S/. ${p.price}")
                        Text("Descripción: ${p.description}")
                    }

                    
                }
            }
        }
    }
}
