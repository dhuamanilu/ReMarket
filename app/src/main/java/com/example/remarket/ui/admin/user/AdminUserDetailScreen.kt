package com.example.remarket.ui.admin.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    userId: String,
    onBack: () -> Unit,
    vm: AdminUserDetailViewModel = hiltViewModel()
) {
    val ui = vm.state.collectAsState().value

    LaunchedEffect(userId) { vm.load(userId) }

    Scaffold(topBar = {
        SmallTopAppBar(
            title = { Text("Detalle de Usuario") },
            navigationIcon = { IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null)
            } }
        )
    }) { padd ->
        when {
            ui.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(ui.error)
            }
            ui.user != null -> {
                val u = ui.user
                Column(
                    Modifier
                        .padding(padd)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nombre: ${u.firstName} ${u.lastName}")
                    Text("Correo: ${u.email}")
                    Text("Rol: ${u.role}")
                    Text("Aprobado: ${u.isApproved}")
                }
            }
        }
    }
}
