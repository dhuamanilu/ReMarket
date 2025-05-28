package com.example.remarket.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

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

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableStateFlow<NavigationEvent?>(null)
    val navigationEvent: StateFlow<NavigationEvent?> = _navigationEvent.asStateFlow()

    // Datos de prueba para validación
    private val testUsers = mapOf(
        "admin@remarket.com" to "admin123",
        "user@remarket.com" to "user123",
        "test@example.com" to "password"
    )

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

        // Validar campos
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

        // Simular proceso de login
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            delay(2000)

            // Validar credenciales con datos de prueba
            val isValidUser = testUsers[currentState.email] == currentState.password

            if (isValidUser) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true
                )

                // Determinar tipo de usuario y navegar
                when (currentState.email) {
                    "admin@remarket.com" -> _navigationEvent.value = NavigationEvent.NavigateToAdmin
                    else -> _navigationEvent.value = NavigationEvent.NavigateToHome
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Credenciales incorrectas. Intenta nuevamente."
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
