package com.example.remarket.ui.admin.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.remarket.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserDetailScreen(
    navController: NavHostController,
    userId: String,
    onBack: () -> Unit,
    vm: AdminUserDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        vm.load(userId)
    }
    val ui = vm.state.collectAsState().value
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Detalle de Usuario") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.primary
    ) { paddingValues ->
        when {
            ui.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            ui.error != null -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ui.error,
                    color = MaterialTheme.colorScheme.error
                )
            }
            ui.user != null -> {
                val u = ui.user
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(paddingValues)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                // DNI Images
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    AsyncImage(
                                        model = u.dniFrontUrl,
                                        contentDescription = "DNI Frente",
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(160.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(12.dp)
                                            )
                                    )
                                    AsyncImage(
                                        model = u.dniBackUrl,
                                        contentDescription = "DNI Reverso",
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(160.dp)
                                            .background(
                                                MaterialTheme.colorScheme.surface,
                                                RoundedCornerShape(12.dp)
                                            )
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                // Read-only fields
                                OutlinedTextField(
                                    value = u.firstName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Nombres") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = DisabledColors()
                                )
                                OutlinedTextField(
                                    value = u.lastName,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Apellidos") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = DisabledColors()
                                )
                                OutlinedTextField(
                                    value = u.email,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Correo") },
                                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = DisabledColors()
                                )
                                OutlinedTextField(
                                    value = u.id,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("DNI") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = DisabledColors()
                                )
                                Spacer(Modifier.height(24.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            vm.approve(true) {
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("refresh_pending_users", true)
                                                onBack()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Aceptar")
                                    }
                                    Button(
                                        onClick = {
                                            vm.approve(false) {
                                                navController.previousBackStackEntry
                                                    ?.savedStateHandle
                                                    ?.set("refresh_pending_users", true)
                                                onBack()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        )
                                    ) {
                                        Text("Rechazar")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DisabledColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    disabledBorderColor = Color.Transparent,
    disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
)
