package com.example.remarket.ui.myproducts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

import com.example.remarket.data.model.Product

@Composable
fun MyProductsScreen(uiState: MyProductsViewModel.UiState, paddingValues: PaddingValues) {
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
            items(uiState.products) { p -> ProductRow(p) }
        }
    }
}

@Composable
private fun ProductRow(p: Product) = Card(Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp)) {
    Column(Modifier.padding(16.dp)) {
        Text("${p.brand} ${p.model}", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(4.dp))
        Text("Estado: ${p.status.uppercase()}")
    }
}
