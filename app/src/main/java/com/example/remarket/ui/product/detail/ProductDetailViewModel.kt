// ui/product/detail/ProductDetailViewModel.kt
package com.example.remarket.ui.product.detail

import androidx.core.i18n.DateTimeFormatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth // <-- AÑADIDO
import kotlinx.coroutines.flow.update


// El Data Class no necesita cambios
data class ProductDetailUiState(
    val product: Product? = null,
    val sellerName: String? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val error: String? = null,
    val showReportDialog: Boolean = false,
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false,
    val isOwner: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val deleteMessage: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    private val userRepo: UserRepository,
    private val firebaseAuth: FirebaseAuth // Se inyecta FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _isProductLoaded = MutableStateFlow(false)
    val isProductLoaded: StateFlow<Boolean> = _isProductLoaded.asStateFlow()

    // --- LÓGICA MODIFICADA PARA SER MÁS ROBUSTA ---

    // 1. Creamos un AuthStateListener. Este se activará cuando la sesión del usuario cambie (inicie o cierre).
    private val authStateListener = FirebaseAuth.AuthStateListener { auth ->
        // Cada vez que el estado de autenticación cambia, re-verificamos si el usuario es el dueño.
        checkOwnership()
    }

    init {
        // 2. Adjuntamos el listener al ViewModel cuando se crea.
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun onCleared() {
        // 3. Quitamos el listener cuando el ViewModel se destruye para evitar fugas de memoria.
        super.onCleared()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

    // 4. Nueva función privada para centralizar la lógica de verificación de propiedad.
    private fun checkOwnership() {
        val product = _uiState.value.product
        val currentUser = firebaseAuth.currentUser

        if (product != null && currentUser != null) {
            val isOwner = product.sellerId == currentUser.uid
            _uiState.update { it.copy(isOwner = isOwner) }
        } else {
            _uiState.update { it.copy(isOwner = false) }
        }
    }

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, isOwner = false) }
            productRepository.getProductById(productId)
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val product = resource.data
                            _uiState.update { it.copy(product = product, isLoading = false) }
                            _isProductLoaded.value = true

                            // 5. Llamamos a checkOwnership() aquí, después de que el producto se carga.
                            checkOwnership()

                            // La lógica para obtener el nombre del vendedor se mantiene igual
                            if (product != null) {
                                val userResult = userRepo.getUserById(product.sellerId)
                                if (userResult is Resource.Success) {
                                    val sellerName = "${userResult.data.firstName} ${userResult.data.lastName}"
                                    _uiState.update { it.copy(sellerName = sellerName) }
                                }
                            }
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    product = null,
                                    isLoading = false,
                                    error = resource.message ?: "Error desconocido"
                                )
                            }
                            _isProductLoaded.value = false
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                        else -> {}
                    }
                }
        }
    }

    // --- EL RESTO DE FUNCIONES NO NECESITA CAMBIOS ---

    fun onDeleteClicked() {
        _uiState.update { it.copy(showDeleteConfirmDialog = true, deleteMessage = null) }
    }

    fun onDismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteConfirmDialog = false) }
    }


    fun onConfirmDelete() {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showDeleteConfirmDialog = false) }
            when (val result = productRepository.deleteProduct(productId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, deleteMessage = "Producto eliminado.") }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, deleteMessage = result.message) }
                }
                else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }

    fun toggleFavorite() {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId).collect { success ->
                if (success) {
                    _uiState.update { it.copy(isFavorite = !it.isFavorite) }
                }
            }
        }
    }

    fun showReportDialog() {
        _uiState.update { it.copy(showReportDialog = true) }
    }

    fun hideReportDialog() {
        _uiState.update { it.copy(showReportDialog = false, reportSuccess = false) }
    }

    fun reportProduct(reason: String) {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isReporting = true) }
            productRepository.reportProduct(productId, reason).collect { success ->
                _uiState.update {
                    it.copy(
                        isReporting = false,
                        reportSuccess = success,
                        showReportDialog = !success
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
