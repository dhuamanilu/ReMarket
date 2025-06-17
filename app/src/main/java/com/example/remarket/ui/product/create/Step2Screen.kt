package com.example.remarket.ui.product.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Segunda pantalla: captura IMEI y descripción del producto.
 */
@Composable
fun Step2Screen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    // Estado de IMEI y descripción
    val imei by viewModel.imei.collectAsState()
    val description by viewModel.description.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Detalles del producto",
            style = MaterialTheme.typography.titleLarge
        )

        // Campo IMEI
        OutlinedTextField(
            value = imei,
            onValueChange = viewModel::onImeiChanged,
            label = { Text("IMEI (obligatorio)") },
            placeholder = { Text("123456789012345") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Campo descripción
        OutlinedTextField(
            value = description,
            onValueChange = viewModel::onDescriptionChanged,
            label = { Text("Descripción") },
            placeholder = { Text("Agrega detalles del estado, defectos, accesorios...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) {
                Text("Atrás")
            }
            Button(onClick = onNext) {
                Text("Siguiente")
            }
        }
    }
}