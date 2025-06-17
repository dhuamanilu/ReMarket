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

// Estado de la UI en detalle de producto
data class ProductDetailUiState(
    val product: Product? = null,
    val sellerName: String? = null,
    val isLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val error: String? = null,
    val showReportDialog: Boolean = false,
    val isReporting: Boolean = false,
    val reportSuccess: Boolean = false
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    private val userRepo: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _isProductLoaded = MutableStateFlow(false)
    val isProductLoaded: StateFlow<Boolean> = _isProductLoaded.asStateFlow()

    /** Carga un producto por ID manejando Resource desde el repositorio */
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            productRepository.getProductById(productId)
                .collect { resource ->
                    when (resource) {

                        is Resource.Success -> {
                            val product = resource.data
                            var sellerName = ""
                            if (product != null) {
                                val userResult = userRepo.getUserById(product.sellerId)
                                if (userResult is Resource.Success) {
                                    sellerName = userResult.data.name
                                }
                            }
                            _uiState.value = _uiState.value.copy(
                                product = product,
                                sellerName = sellerName,
                                isLoading = false
                            )
                            _isProductLoaded.value = true
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                product = null,
                                isLoading = false,
                                error = resource.message ?: "Error desconocido"
                            )
                            _isProductLoaded.value = false
                        }
                        is Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = true
                            )
                        }

                        is Resource.Idle -> {
                            //nda
                        }
                    }
                }
        }
    }

    fun toggleFavorite() {
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            productRepository.toggleFavorite(productId)
                .collect { success ->
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            isFavorite = !_uiState.value.isFavorite
                        )
                    }
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
        val productId = _uiState.value.product?.id ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isReporting = true)
            productRepository.reportProduct(productId, reason)
                .collect { success ->
                    _uiState.value = _uiState.value.copy(
                        isReporting = false,
                        reportSuccess = success,
                        showReportDialog = !success
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

}
