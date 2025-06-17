// File: app/src/main/java/com/example/remarket/ui/product/create/Step3Screen.kt
package com.example.remarket.ui.product.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem
@Composable
fun Step3Screen(
    viewModel: CreateProductViewModel = hiltViewModel(),
    onNext: () -> Unit,
    onBack: () -> Unit = {}
) {
    val boxUrl by viewModel.boxImageUrl.collectAsState()
    val invoiceUrl by viewModel.invoiceUrl.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Adjunta caja y boleta", style = MaterialTheme.typography.titleLarge)

        Text("Imagen de la caja")
        ImagePickerItem(
            imageUri = boxUrl.takeIf { it.isNotBlank() },
            size = 150.dp,
            onPick = { viewModel.setBoxImage(it) }
        )

        Text("Factura o boleta")
        ImagePickerItem(
            imageUri = invoiceUrl.takeIf { it.isNotBlank() },
            size = 150.dp,
            onPick = { viewModel.setInvoiceImage(it) }
        )

        Spacer(modifier = Modifier.weight(1f))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("Atrás") }
            Button(onClick = onNext) { Text("Siguiente") }
        }
    }
}