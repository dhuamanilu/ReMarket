// ui/admin/review/AdminReviewStep2Screen.kt
package com.example.remarket.ui.admin.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import com.example.remarket.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun AdminReviewStep2Screen(
    viewModel: AdminReviewViewModel,
    navController: NavHostController  // ① pásalo desde AppNavGraph// ← volver al listado
) {
    val images      by viewModel.images.collectAsState()
    val boxImageUrl by viewModel.boxImageUrl.collectAsState()
    val invoiceUrl  by viewModel.invoiceUrl.collectAsState()
    val scope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Fotos del producto", style = MaterialTheme.typography.titleLarge)

        // Galería principal
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images, key = { it }) { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (boxImageUrl.isNotBlank()) {
            Text("Foto de la caja", style = MaterialTheme.typography.titleMedium)
            AsyncImage(
                model = boxImageUrl,
                contentDescription = "Caja",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        if (invoiceUrl.isNotBlank()) {
            Text("Factura / Comprobante", style = MaterialTheme.typography.titleMedium)
            AsyncImage(
                model = invoiceUrl,
                contentDescription = "Factura",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(Modifier.weight(1f))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.approve()) {
                            // 🔸 avisa a la entrada anterior que debe refrescar
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refreshPending", true)
                            navController.popBackStack(Routes.ADMIN_HOME, false)
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) { Text("Aceptar") }

            Button(
                onClick = {
                    scope.launch {
                        if (viewModel.reject()) {
                            // 🔸 avisa a la entrada anterior que debe refrescar
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("refreshPending", true)
                            navController.popBackStack(Routes.ADMIN_HOME, false)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.weight(1f)
            ) { Text("Rechazar") }
        }
    }
}

