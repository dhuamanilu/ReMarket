package com.example.remarket.ui.profile
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(state: ProfileViewModel.UiState, padding: PaddingValues) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(padding), contentAlignment = Alignment.Center) {
        when {
            state.isLoading -> {
                CircularProgressIndicator()
            }
            state.error != null -> {
                Text(text = state.error ?: "Error", color = MaterialTheme.colorScheme.error)
            }
            state.user != null -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.user.firstName} ${state.user.lastName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = state.user.email)
                    Spacer(Modifier.height(8.dp))
                    Text(text = "Rol: ${state.user.role}")
                }
            }
        }
    }
}
