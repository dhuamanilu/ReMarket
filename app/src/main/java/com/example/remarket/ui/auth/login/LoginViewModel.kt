package com.example.remarket.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.network.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.remarket.data.repository.UserRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,
    val isEmailValid: Boolean = true,
    val isPasswordValid: Boolean = true
)

@HiltViewModel // <-- Asegúrate que esta anotación esté
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth, // Inyecta FirebaseAuth
    private val tokenManager: TokenManager    // Inyecta nuestro TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            errorMessage = null,
            isEmailValid = validateEmail(email)
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            errorMessage = null,
            isPasswordValid = validatePassword(password)
        )
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            isPasswordVisible = !_uiState.value.isPasswordVisible
        )
    }

    fun onLoginClicked() {
        val currentState = _uiState.value
        val isEmailValid = validateEmail(currentState.email)
        val isPasswordValid = validatePassword(currentState.password)

        _uiState.value = currentState.copy(
            isEmailValid = isEmailValid,
            isPasswordValid = isPasswordValid
        )

        if (!isEmailValid || !isPasswordValid) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Por favor, verifica que todos los campos sean válidos"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                // Inicia sesión con Firebase en un hilo de background
                val authResult = withContext(Dispatchers.IO) {
                    Tasks.await(firebaseAuth.signInWithEmailAndPassword(currentState.email, currentState.password))
                }

                // Si llegamos aquí, el login fue exitoso
                val user = authResult.user
                if (user != null) {
                    // Obtenemos el ID Token
                    val tokenResult = withContext(Dispatchers.IO) {
                        Tasks.await(user.getIdToken(true))
                    }
                    tokenManager.saveToken(tokenResult.token ?: "")

                    _uiState.value = _uiState.value.copy(isLoading = false, isLoginSuccessful = true)

                    // Aquí puedes añadir lógica de roles si la necesitas en el futuro,
                    // por ahora, simplemente navegamos a Home.
                    _navigationEvent.value = NavigationEvent.NavigateToHome

                } else {
                    // Caso muy raro donde el login es exitoso pero el usuario es nulo
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No se pudo obtener la información del usuario.")
                }

            } catch (e: Exception) {
                // Capturamos la excepción de Firebase y mostramos un mensaje amigable
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Credenciales incorrectas o el usuario no existe."
                )
            }
        }
    }


    fun onForgotPasswordClicked() {
        _navigationEvent.value = NavigationEvent.NavigateToForgotPassword
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }

    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateEmail(email: String): Boolean {
        return email.isNotBlank() &&
               email.contains("@") &&
               email.contains(".") &&
               email.length >= 5
    }

    private fun validatePassword(password: String): Boolean {
        return password.isNotBlank() && password.length >= 6
    }
}

sealed class NavigationEvent {
    object NavigateToHome : NavigationEvent()
    object NavigateToAdmin : NavigationEvent()
    object NavigateToForgotPassword : NavigationEvent()
}
