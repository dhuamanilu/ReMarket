package com.example.remarket.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth // <-- AÑADE ESTE IMPORT


data class PendingUiState(
    val products: List<com.example.remarket.data.model.Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// ui/admin/AdminPendingProductsViewModel.kt

@HiltViewModel
class AdminPendingProductsViewModel @Inject constructor(
    private val repo: IProductRepository,
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableStateFlow(PendingUiState())
    val state: StateFlow<PendingUiState> = _state.asStateFlow()

    init { load() }
    fun onLogout() {
        firebaseAuth.signOut()
        tokenManager.clearToken()
    }
    fun load() = viewModelScope.launch {
        repo.getPendingProductsFromFirebase().collect { res ->
            when (res) {
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, products = res.data, error = null)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, error = res.message)
                }

                Resource.Idle -> TODO()
                else -> {}
            }
        }
    }

    fun approve(id: String)  = setStatus(id, "approved")
    fun reject(id: String)   = setStatus(id, "rejected")

    private fun setStatus(id: String, s: String) = viewModelScope.launch {
        val res = repo.updateProductStatus(id, s)
        if (res is Resource.Success) {
            load() // recarga lista para reflejar cambios
        } else if (res is Resource.Error) {
            _state.update { it.copy(error = res.message) }
        }
    }
}

data class ProductDetailUiState(
    val product: com.example.remarket.data.model.Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminProductDetailViewModel @Inject constructor(
    private val repo: IProductRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ProductDetailUiState())
    val state: StateFlow<ProductDetailUiState> = _state.asStateFlow()

    fun loadProduct(id: String) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }

        val result = repo.getProductByIdFromFirebase(id)
        when (result) {
            is Resource.Success -> _state.update {
                it.copy(product = result.data, isLoading = false)
            }
            is Resource.Error -> _state.update {
                it.copy(error = result.message, isLoading = false)
            }
            else -> {}
        }
    }
}
