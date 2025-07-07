package com.example.remarket.ui.myproducts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.remarket.data.model.Product

@HiltViewModel
class MyProductsViewModel @Inject constructor(
    private val repo: IProductRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    data class UiState(
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        repo.getAllProducts().collect { res ->
            when (res) {
                is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Resource.Success -> {
                    val currentUid = auth.currentUser?.uid

                    // 1) Recorremos cada producto y logueamos la comparación
                    res.data.forEach { p ->
                        val matches = p.sellerId == currentUid
                        Log.d(
                            "ProductosViewModel",
                            "Comparando p.sellerId='${p.sellerId}' con uid='$currentUid' → coincide? $matches"
                        )
                    }

                    // 2) Hacemos el filter y logueamos la lista resultante
                    val filtered = res.data.filter { it.sellerId == currentUid }
                    Log.d("ProductosViewModel", "Productos filtrados (${filtered.size}): $filtered")

                    // 3) Actualizamos el estado con el filtrado
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = filtered
                        )
                    }
                }

                is Resource.Error   -> _uiState.update { it.copy(isLoading = false, error = res.message) }
                else -> {}
            }
        }
    }
}
