// Register2Screen.kt
package com.example.remarket.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.common.ImagePickerItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register2Screen(
    onBack: () -> Unit,
    onRegister: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    // La lógica de negocio y estado se mantiene intacta
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirm by viewModel.confirmPassword.collectAsState()
    val dniFrontUri by viewModel.dniFrontImageUri.collectAsState()
    val dniBackUri by viewModel.dniBackImageUri.collectAsState()

    // Los diálogos de éxito y error se mantienen igual, ya que son modales sobre la UI
    if (uiState.isRegistrationSuccessful) {
        AlertDialog(
            onDismissRequest = { /* No permitir cerrar */ },
            title = { Text("¡Registro Exitoso!") },
            text = { Text("Tu cuenta ha sido creada. Serás redirigido para iniciar sesión.") },
            confirmButton = {
                TextButton(onClick = onRegister) {
                    Text("Aceptar")
                }
            }
        )
    }

    uiState.errorMessage?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearErrorMessage() },
            title = { Text("Error en el Registro") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearErrorMessage() }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Contenedor principal con el fondo de gradiente
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Cabecera con estilo consistente
            Register2Header()
            Spacer(Modifier.height(32.dp))

            // Selector de imagen frontal DNI
            Text(
                text = "Foto Frontal del DNI",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)), // Estilo consistente
                contentAlignment = Alignment.Center
            ) {
                // El componente ImagePickerItem se reutiliza sin cambios internos
                ImagePickerItem(
                    imageUri = dniFrontUri,
                    size = 150.dp,
                    onPick = viewModel::onDniFrontImageSelected
                )
            }
            Spacer(Modifier.height(16.dp))

            // Selector de imagen trasera DNI
            Text(
                text = "Foto Trasera del DNI",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.White, RoundedCornerShape(12.dp)), // Estilo consistente
                contentAlignment = Alignment.Center
            ) {
                ImagePickerItem(
                    imageUri = dniBackUri,
                    size = 150.dp,
                    onPick = viewModel::onDniBackImageSelected
                )
            }
            Spacer(Modifier.height(24.dp))

            // Campo de Correo
            Text(
                text = "Correo electrónico",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                placeholder = { Text("Ingresa tu correo", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Correo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Campo de Contraseña
            Text(
                text = "Contraseña",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChanged,
                placeholder = { Text("Crea una contraseña", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Campo de Confirmar Contraseña
            Text(
                text = "Confirmar contraseña",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = confirm,
                onValueChange = viewModel::onConfirmPasswordChanged,
                placeholder = { Text("Confirma tu contraseña", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Confirmar") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true
            )
            Spacer(Modifier.height(32.dp))

            // Botones de navegación
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón Regresar
                Button(
                    onClick = onBack,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Regresar", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }

                // Botón Registrarse (con indicador de carga)
                Button(
                    onClick = { viewModel.onRegisterClicked() },
                    enabled = !uiState.isLoading, // Se mantiene la lógica de habilitación
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6366F1),
                        disabledContainerColor = Color.White.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF6366F1) // Color del indicador
                        )
                    } else {
                        Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}


@Composable
private fun Register2Header() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(40.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock, // Icono relevante para seguridad
                contentDescription = "Icono de Seguridad",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Último Paso",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Verificación y acceso",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}