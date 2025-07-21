package com.example.remarket.ui.admin.reports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageReportsScreen(
    viewModel: ManageReportsViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onNavigateToProduct: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Reportes") }) },
        modifier = Modifier.padding(paddingValues)
    ) { inner ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }

            state.error != null -> ErrorSection(state.error!!) { viewModel.clearError(); viewModel.refresh() }

            else -> {
                var reportToDelete by remember { mutableStateOf<ReportItem?>(null) }

                // Diálogo de confirmación
                if (reportToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { reportToDelete = null },
                        title = { Text("Eliminar reporte") },
                        text = { Text("¿Seguro que deseas borrar este reporte?") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.delete(reportToDelete!!.report)
                                reportToDelete = null
                            }) { Text("Eliminar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { reportToDelete = null }) { Text("Cancelar") }
                        }
                    )
                }

                LazyColumn(contentPadding = inner) {
                    items(state.items, key = { it.report.id }) { item ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { onNavigateToProduct(item.report.productId) }
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(item.productName, style = MaterialTheme.typography.titleMedium)
                                Text("Motivo: ${item.report.reason}")
                                Text(item.reporterEmail, style = MaterialTheme.typography.bodySmall)
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(
                                        enabled = state.deletingId != item.report.id,
                                        onClick = { reportToDelete = item } // ✅ Confirmación ahora
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }
}


@Composable
private fun ErrorSection(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
