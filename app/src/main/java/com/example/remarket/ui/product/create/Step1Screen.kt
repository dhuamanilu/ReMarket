// File: app/src/main/java/com/example/remarket/ui/product/create/Step1Screen.kt
package com.example.remarket.ui.product.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun Step1Screen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onNext: () -> Unit
) {
    val brand by viewModel.brand.collectAsState()
    val model by viewModel.model.collectAsState()
    val storage by viewModel.storage.collectAsState()
    val price by viewModel.price.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Comienza con tu venta", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = brand, onValueChange = viewModel::onBrandChanged, label = { Text("Marca") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = model, onValueChange = viewModel::onModelChanged, label = { Text("Modelo") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = storage, onValueChange = viewModel::onStorageChanged, label = { Text("Almacenamiento") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = price.toString(), onValueChange = { it.toDoubleOrNull()?.let(viewModel::onPriceChanged) }, label = { Text("Precio (S/)") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onNext, modifier = Modifier.fillMaxWidth()) { Text("Siguiente") }
    }
}