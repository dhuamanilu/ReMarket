package com.example.remarket.ui.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    navController: NavHostController,   // ⬅️  nuevo parámetro
    userId: String,
    onBack: () -> Unit,
    vm: AdminUserDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) { vm.load(userId) }

    val ui = vm.state.collectAsState().value
    val scroll = rememberScrollState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Detalle de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.Person, null) }
                }
            )
        }
    ) { padd ->
        when {
            ui.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            ui.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(ui.error)
            }
            ui.user != null -> {
                val u = ui.user
                // ---------- UI re-usando estilo de registro ----------
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padd)
                        .verticalScroll(scroll)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // IMÁGENES DNI
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = u.dniFrontUrl,
                            contentDescription = "DNI Frente",
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                        )
                        AsyncImage(
                            model = u.dniBackUrl,
                            contentDescription = "DNI Reverso",
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // CAMPOS – mismos colores pero deshabilitados
                    OutlinedTextField(
                        value = u.firstName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nombres") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = DisabledColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = u.lastName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Apellidos") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = DisabledColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = u.email,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Correo") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = DisabledColors()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = u.id,            // DNI en tu DTO está como id ó dniNumber
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("DNI") },
                        leadingIcon = { Icon(Icons.Default.Badge, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = DisabledColors()
                    )
                    Spacer(Modifier.height(32.dp))

                    // -------- Botones Aceptar / Rechazar --------
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                vm.approve(true) {
                                    // 1️⃣  marca que debe refrescar
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refresh_pending_users", true)
                                    onBack()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Aceptar") }

                        Button(
                            onClick = {
                                vm.approve(false) {
                                    // 1️⃣  marca que debe refrescar
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("refresh_pending_users", true)
                                    onBack()
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
        }
    }
}

/* ---------- helper ---------- */
@Composable
private fun DisabledColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor   = Color.White,
    unfocusedContainerColor = Color.White,
    focusedBorderColor      = Color.Transparent,
    unfocusedBorderColor    = Color.Transparent,
    disabledTextColor       = Color.Black,
    disabledLabelColor      = Color.Gray,
    disabledLeadingIconColor = Color.Gray,
    disabledBorderColor     = Color.Transparent
).copy(
    disabledContainerColor = Color.White
)
