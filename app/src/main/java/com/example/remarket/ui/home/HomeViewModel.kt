// ui/home/HomeViewModel.kt
package com.example.remarket.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProducts: GetProductsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadProducts() }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                getProducts().collect { list ->
                    _uiState.update {
                        it.copy(
                            products = list,
                            filteredProducts = filter(list, it.searchQuery),
                            isLoading = false
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    fun onSearchQueryChanged(q: String) {
        _uiState.update {
            it.copy(
                searchQuery = q,
                filteredProducts = filter(it.products, q)
            )
        }
    }

    private fun filter(list: List<Product>, q: String) =
        if (q.isBlank()) list
        else list.filter {
            it.brand.contains(q, true) ||
                    it.model.contains(q, true)  ||
                    it.storage.contains(q, true)
        }
}
