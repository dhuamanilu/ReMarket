package com.example.remarket.ui.product.create

import ads_mobile_sdk.h6
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateProductScreen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    // Estados del ViewModel
    val brand by viewModel.brand.collectAsState()
    val modelText by viewModel.modelText.collectAsState()
    val storageText by viewModel.storageText.collectAsState()
    val price by viewModel.price.collectAsState()

    val isPosting by viewModel.isPosting.collectAsState()
    val error by viewModel.error.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Comienza con tu venta", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = brand,
                onValueChange = viewModel::onBrandChanged,
                label = { Text("Marca") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = modelText,
                onValueChange = viewModel::onModelChanged,
                label = { Text("Modelo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = storageText,
                onValueChange = viewModel::onStorageChanged,
                label = { Text("Almacenamiento") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = viewModel::onPriceChanged,
                label = { Text("Precio (S/)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            if (!error.isNullOrEmpty()) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = { viewModel.submit(onSuccess = onNext) },
                enabled = !isPosting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isPosting) "Publicando..." else "Siguiente")
            }
        }
    }
}
