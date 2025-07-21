package com.example.remarket.ui.admin.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.remarket.ui.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPendingUsersScreen(
    navController: NavHostController,   // ⬅️  pásalo desde el NavGraph
    vm: AdminPendingUsersViewModel = hiltViewModel(),
    onUserClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    // 1️⃣  Escucha el flag
    val shouldRefresh = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("refresh_pending_users", false)
        ?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    // 2️⃣  Si llega en true => recarga y lo pones en false
    LaunchedEffect(shouldRefresh.value) {
        if (shouldRefresh.value) {
            vm.load()                                    // 🔄
            navController.currentBackStackEntry
                ?.savedStateHandle
                ?.set("refresh_pending_users", false)
        }
    }
    val ui = vm.state.collectAsState().value

    Scaffold(topBar = {
        SmallTopAppBar(
            title = { Text("Usuarios por aprobar") },
            actions = {
                TextButton(onClick = onLogout) { Text("Salir") }
            })
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
                items(ui.users) { u ->
                    Card(Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)) {

                        Column(Modifier.padding(16.dp)) {
                            Text("${u.firstName} ${u.lastName}")
                            Text(u.email, style = MaterialTheme.typography.bodySmall)

                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { vm.approve(u.id) }) {
                                    Text("Aceptar")
                                }
                                Button(
                                    onClick = { vm.reject(u.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) { Text("Rechazar") }
                                TextButton(onClick = { onUserClick(u.id) }) {
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
