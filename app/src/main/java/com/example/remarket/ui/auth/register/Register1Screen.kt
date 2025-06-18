// Register1Screen.kt
package com.example.remarket.ui.auth.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Register1Screen(
    onBack: () -> Unit,
    onNext: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    // Se mantiene la lógica del estado intacta
    val scrollState = rememberScrollState()
    val firstName by viewModel.firstName.collectAsState()
    val lastName by viewModel.lastName.collectAsState()
    val dni by viewModel.dni.collectAsState()
    val phone by viewModel.phone.collectAsState()

    // Contenedor principal con el fondo de gradiente, igual que en Login
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
                .verticalScroll(scrollState), // Se mantiene el scroll para el formulario
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Sección de cabecera con estilo consistente
            RegisterHeader()

            Spacer(modifier = Modifier.height(32.dp))

            // ----- Formulario de Registro - Parte 1 -----

            // Campo de Nombres
            Text(
                text = "Nombres",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = firstName,
                onValueChange = viewModel::onFirstNameChanged,
                placeholder = { Text("Ingresa tus nombres", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombres") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Campo de Apellidos
            Text(
                text = "Apellidos",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = viewModel::onLastNameChanged,
                placeholder = { Text("Ingresa tus apellidos", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Apellidos") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Campo de DNI
            Text(
                text = "DNI",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = dni,
                onValueChange = viewModel::onDniChanged,
                placeholder = { Text("Ingresa tu DNI", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "DNI") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))

            // Campo de Teléfono
            Text(
                text = "Teléfono",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = phone,
                onValueChange = viewModel::onPhoneChanged,
                placeholder = { Text("Ingresa tu teléfono", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )
            Spacer(Modifier.height(32.dp))

            // Botones de navegación con nuevo estilo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón para regresar (estilo secundario)
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        "Regresar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Botón para siguiente (estilo primario)
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6366F1)
                    )
                ) {
                    Text(
                        "Siguiente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Círculo de fondo para el ícono, igual que en Login
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(40.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Icono de Registro",
                modifier = Modifier.size(40.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Títulos adaptados para el registro
        Text(
            text = "Crea tu Cuenta",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Text(
            text = "Paso 1 de 2: Datos Personales",
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}