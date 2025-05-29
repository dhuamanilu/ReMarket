// ui/product/detail/ProductDetailViewModel.kt
package com.example.remarket.ui.product.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val error: String? = null,
    val showReportDialog: Boolean = false,
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false
)

class ProductDetailViewModel(
    private val productRepository: ProductRepository = ProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _isProductLoaded = MutableStateFlow(false)
    val isProductLoaded: StateFlow<Boolean> = _isProductLoaded.asStateFlow()

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                productRepository.getProductById(productId).collect { product ->
                    _uiState.value = _uiState.value.copy(
                        product = product,
                        isLoading = false,
                        error = if (product == null) "Producto no encontrado" else null
                    )
                    _isProductLoaded.value = (product != null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el producto: ${e.message}"
                )
                _isProductLoaded.value = false
            }
        }
    }

    fun toggleFavorite() {
        val currentProduct = _uiState.value.product ?: return

        viewModelScope.launch {
            try {
                productRepository.toggleFavorite(currentProduct.id).collect { success ->
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            isFavorite = !_uiState.value.isFavorite
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar favoritos"
                )
            }
        }
    }

    fun showReportDialog() {
        _uiState.value = _uiState.value.copy(showReportDialog = true)
    }

    fun hideReportDialog() {
        _uiState.value = _uiState.value.copy(
            showReportDialog = false,
            reportSuccess = false
        )
    }

    fun reportProduct(reason: String) {
        val currentProduct = _uiState.value.product ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReporting = true)

            try {
                productRepository.reportProduct(currentProduct.id, reason).collect { success ->
                    _uiState.value = _uiState.value.copy(
                        isReporting = false,
                        reportSuccess = success,
                        showReportDialog = !success
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isReporting = false,
                    error = "Error al reportar producto"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
