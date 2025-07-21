// ui/admin/review/AdminReviewStep1Screen.kt
package com.example.remarket.ui.admin.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AdminReviewStep1Screen(
    viewModel: AdminReviewViewModel = hiltViewModel(),
    onNext: () -> Unit            // ⬅️ nuevo parámetro
) {
    val brand        by viewModel.brand.collectAsState()
    val model        by viewModel.model.collectAsState()
    val storage      by viewModel.storage.collectAsState()
    val price        by viewModel.price.collectAsState()
    val imei         by viewModel.imei.collectAsState()
    val description  by viewModel.description.collectAsState()

    Column(Modifier
        .fillMaxSize()
        .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = brand, onValueChange = {},
            readOnly = true, label = { Text("Marca") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = model, onValueChange = {},
            readOnly = true, label = { Text("Modelo") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = storage, onValueChange = {},
            readOnly = true, label = { Text("Almacenamiento") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = price.toString(), onValueChange = {},
            readOnly = true, label = { Text("Precio") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = imei, onValueChange = {},
            readOnly = true, label = { Text("IMEI") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description, onValueChange = {},
            readOnly = true, label = { Text("Descripción") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        Spacer(Modifier.weight(1f))                // empuja el botón al fondo

        // ─── Botón Siguiente ──────────────────────────
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Siguiente") }
    }
}
