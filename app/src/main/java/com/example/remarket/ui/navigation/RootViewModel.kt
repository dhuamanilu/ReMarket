package com.example.remarket.ui.navigation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Las clases de estado y destino no cambian
sealed class NavigationTarget {
    object Loading : NavigationTarget()
    object Login : NavigationTarget()
    object Home : NavigationTarget()
    object Admin : NavigationTarget()
}

data class RootUiState(
    val target: NavigationTarget = NavigationTarget.Loading
)

@HiltViewModel
class RootViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RootUiState())
    val uiState: StateFlow<RootUiState> = _uiState.asStateFlow()

    // Usaremos un tag específico para encontrar los logs fácilmente
    private val TAG = "REMARKET_DEBUG"

    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        val user = auth.currentUser
        if (user == null) {
            Log.e(TAG, "AuthListener: No hay usuario. Navegando a HOME como invitado.")
            _uiState.update { it.copy(target = NavigationTarget.Home) }
        } else {
            Log.d(
                TAG,
                "AuthListener: Usuario detectado (UID: ${user.uid}). Iniciando verificación de rol."
            )
            checkUserRole(user)
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    private fun checkUserRole(user: FirebaseUser) {
        viewModelScope.launch {
            // Envolvemos toda la lógica en un bloque que se ejecutará en un hilo de I/O
            withContext(Dispatchers.IO) {
                try {
                    // Paso 1: Obtener el token de API para nuestro backend.
                    Log.d(TAG, "Paso 1: Obteniendo token de API...")
                    val token = Tasks.await(user.getIdToken(true))?.token
                    if (token.isNullOrBlank()) {
                        Log.e(TAG, "FALLO: El token de Firebase es nulo o vacío. Navegando a HOME.")
                        _uiState.update { it.copy(target = NavigationTarget.Home) } // Fallback
                        return@withContext // Salimos del bloque withContext
                    }
                    tokenManager.saveToken(token)
                    Log.d(TAG, "ÉXITO: Token obtenido y guardado en TokenManager.")

                    // Paso 2: Obtener el perfil del backend para saber el rol.
                    Log.d(TAG, "Paso 2: Llamando a userRepository.getMyProfile()...")
                    when (val profileResult = userRepository.getMyProfile()) {
                        is Resource.Success -> {
                            val userRole = profileResult.data.role
                            Log.d(
                                TAG,
                                "ÉXITO: Perfil obtenido del backend. Rol recibido: '$userRole'"
                            )
                            if (userRole.equals("admin", ignoreCase = true)) {
                                Log.d(TAG, "DECISIÓN: El rol es 'admin'. Navegando a ADMIN_HOME.")
                                _uiState.update { it.copy(target = NavigationTarget.Admin) }
                            } else {
                                Log.d(TAG, "DECISIÓN: El rol es de usuario. Navegando a HOME.")
                                _uiState.update { it.copy(target = NavigationTarget.Home) }
                            }
                        }

                        is Resource.Error -> {
                            Log.e(
                                TAG,
                                "FALLO: userRepository.getMyProfile() devolvió un error: ${profileResult.message}. Navegando a HOME."
                            )
                            _uiState.update { it.copy(target = NavigationTarget.Home) } // Fallback
                        }

                        else -> {}
                    }
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "FALLO: Excepción en checkUserRole: ${e.message}. Navegando a HOME.",
                        e
                    )
                    _uiState.update { it.copy(target = NavigationTarget.Home) } // Fallback
                }
            }
        }
    }
}