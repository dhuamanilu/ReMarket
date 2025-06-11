// ui/home/HomeViewModel.kt
package com.example.remarket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
@HiltViewModel // Añadir esta anotación
class HomeViewModel @Inject constructor( // Modificar constructor para inyección
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { // Cargar productos al iniciar el ViewModel
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                productRepository.getAllProducts().collect { products ->
                    _uiState.value = _uiState.value.copy(
                        products = products,
                        filteredProducts = filterProducts(products, _uiState.value.searchQuery),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar productos: ${e.message}"
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredProducts = filterProducts(_uiState.value.products, query)
        )
    }

    private fun filterProducts(products: List<Product>, query: String): List<Product> {
        if (query.isBlank()) return products

        return products.filter { product ->
            product.brand.contains(query, ignoreCase = true) ||
                    product.model.contains(query, ignoreCase = true) ||
                    product.storage.contains(query, ignoreCase = true)
        }
    }
}