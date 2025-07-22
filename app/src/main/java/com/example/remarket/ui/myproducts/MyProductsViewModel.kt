package com.example.remarket.ui.myproducts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyProductsViewModel @Inject constructor(
    private val repo: IProductRepository
) : ViewModel() {

    data class UiState(
        val products: List<Product> = emptyList(),
        val bought:   List<Product> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _ui

    init { load() }

    private fun load() {
        viewModelScope.launch {
            // 1️⃣ Carga tus productos en venta
            launch {
                repo.getMyProducts().collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            _ui.update { it.copy(isLoading = true) }
                        }
                        is Resource.Success -> {
                            val list = res.data ?: emptyList()
                            Log.d("MyProductsVM", "Products FOR SALE (${list.size}): $list")
                            _ui.update { it.copy(isLoading = false, products = list) }
                        }
                        is Resource.Error -> {
                            _ui.update { it.copy(isLoading = false, error = res.message) }
                        }
                        Resource.Idle -> { /* no-op */ }
                    }
                }
            }
            // 2️⃣ Carga tus productos comprados
            launch {
                repo.getMyPurchases().collect { res ->
                    when (res) {
                        is Resource.Loading -> {
                            // opcional si quieres log de loading compras
                        }
                        is Resource.Success -> {
                            val list = res.data ?: emptyList()
                            Log.d("MyProductsVM", "Products BOUGHT   (${list.size}): $list")
                            _ui.update { it.copy(bought = list) }
                        }
                        is Resource.Error -> {
                            _ui.update { it.copy(error = res.message) }
                        }
                        Resource.Idle -> { /* no-op */ }
                    }
                }
            }
        }
    }
}
