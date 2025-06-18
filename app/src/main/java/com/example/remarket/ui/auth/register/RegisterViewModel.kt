// File: app/src/main/java/com/example/remarket/ui/auth/register/RegisterViewModel.kt
package com.example.remarket.ui.auth.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.repository.CloudinaryService
import com.example.remarket.data.repository.IConnectivityRepository
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource // <-- ¡IMPORTANTE!
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.TimeoutCancellationException

// ... (Las data classes RegisterUiState y ValidationErrors se mantienen igual)
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
    private val userRepository: UserRepository,
    private val cloudinaryService: CloudinaryService,
    private val connectivityRepository: IConnectivityRepository,
    @ApplicationContext private val context: Context
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

    private val _dniFrontImageUri = MutableStateFlow("")
    val dniFrontImageUri: StateFlow<String> = _dniFrontImageUri.asStateFlow()

    private val _dniBackImageUri = MutableStateFlow("")
    val dniBackImageUri: StateFlow<String> = _dniBackImageUri.asStateFlow()

    // Métodos de actualización (onFirstNameChanged, etc.) se mantienen igual...
    fun onFirstNameChanged(value: String) { _firstName.value = value }
    fun onLastNameChanged(value: String) { _lastName.value = value }
    fun onDniChanged(value: String) { _dni.value = value.filter { it.isDigit() } }
    fun onPhoneChanged(value: String) { _phone.value = value.filter { it.isDigit() } }
    fun onEmailChanged(value: String) { _email.value = value }
    fun onPasswordChanged(value: String) { _password.value = value }
    fun onConfirmPasswordChanged(value: String) { _confirmPassword.value = value }

    // --- NUEVOS MÉTODOS PARA ACTUALIZAR LAS URIs DE LAS IMÁGENES ---
    fun onDniFrontImageSelected(uri: String) {
        _dniFrontImageUri.value = uri
    }

    fun onDniBackImageSelected(uri: String) {
        _dniBackImageUri.value = uri
    }

    // --- LÓGICA DE onRegisterClicked ACTUALIZADA ---
    fun onRegisterClicked() {
        // Validación básica (se mantiene igual)
        if (password.value != confirmPassword.value) { /* ... */ return }
        if (email.value.isBlank() || password.value.isBlank() || firstName.value.isBlank() || lastName.value.isBlank() || dni.value.isBlank()) { /* ... */ return }
        // Podrías añadir validación para las imágenes si son obligatorias
        if (_dniFrontImageUri.value.isBlank() || _dniBackImageUri.value.isBlank()) { return }
        if (!connectivityRepository.isNetworkAvailable()) {
            _uiState.value = _uiState.value.copy(errorMessage = "No hay conexión a internet. Por favor, verifica tu red.")
            return // Detiene la ejecución aquí mismo
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                withTimeout(30000L) {
                    // 1. Subir imágenes a Cloudinary (si se seleccionaron)
                    val dniFrontUrl = if (_dniFrontImageUri.value.isNotBlank()) {
                        cloudinaryService.uploadImage(context, _dniFrontImageUri.value)
                    } else {
                        null
                    }

                    val dniBackUrl = if (_dniBackImageUri.value.isNotBlank()) {
                        cloudinaryService.uploadImage(context, _dniBackImageUri.value)
                    } else {
                        null
                    }

                    // 2. Llamar al repositorio con las URLs obtenidas
                    val result = userRepository.createUser(
                        firstName = firstName.value,
                        lastName = lastName.value,
                        dni = dni.value,
                        phone = phone.value,
                        email = email.value,
                        password = password.value,
                        dniFrontUrl = dniFrontUrl, // Pasa la URL de Cloudinary
                        dniBackUrl = dniBackUrl      // Pasa la URL de Cloudinary
                    )

                    // 3. Manejar el resultado (esta parte no cambia)
                    when (result) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isRegistrationSuccessful = true
                            )
                        }

                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = result.message
                            )
                        }

                        else -> {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
                }

            } catch (e: TimeoutCancellationException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "La operación tardó demasiado. Revisa tu conexión a internet."
                )
            } catch (e: Exception) {
                // Captura errores de la subida a Cloudinary
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al subir imágenes: ${e.message}"
                )
            }
        }
    }
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}