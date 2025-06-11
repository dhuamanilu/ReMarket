// RegisterViewModel.kt
package com.example.remarket.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.repository.UserRepository // <-- Importa
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRegistrationSuccessful: Boolean = false,
    val validationErrors: ValidationErrors = ValidationErrors()
)

data class ValidationErrors(
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val dniError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository // <-- Inyecta el repositorio
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _firstName = MutableStateFlow("")
    val firstName: StateFlow<String> = _firstName.asStateFlow()

    private val _lastName = MutableStateFlow("")
    val lastName: StateFlow<String> = _lastName.asStateFlow()

    private val _dni = MutableStateFlow("")
    val dni: StateFlow<String> = _dni.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

    // Métodos de actualización
    fun onFirstNameChanged(value: String) {
        _firstName.value = value
    }

    fun onLastNameChanged(value: String) {
        _lastName.value = value
    }

    fun onDniChanged(value: String) {
        _dni.value = value.filter { it.isDigit() }
    }

    fun onPhoneChanged(value: String) {
        _phone.value = value.filter { it.isDigit() }
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun onConfirmPasswordChanged(value: String) {
        _confirmPassword.value = value
    }
    fun onRegisterClicked(onSuccess: () -> Unit) {
        // Validación básica (se puede expandir)
        if (password.value != confirmPassword.value) {
            _uiState.value = _uiState.value.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }
        if (email.value.isBlank() || password.value.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "El email y la contraseña no pueden estar vacíos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val success = userRepository.createUser(email.value, password.value)

            if (success) {
                _uiState.value = _uiState.value.copy(isLoading = false, isRegistrationSuccessful = true)
                onSuccess() // Llama al callback de navegación
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "El correo electrónico ya está en uso."
                )
            }
        }
    }
}