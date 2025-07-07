package com.example.remarket.ui.myproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            repo.getMyProducts().collect { res ->
                when (res) {
                    is Resource.Loading ->
                        _uiState.update { it.copy(isLoading = true) }

                    is Resource.Success ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                products = res.data
                            )
                        }

                    is Resource.Error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = res.message
                            )
                        }
                    else -> { /* no-op */ }
                }
            }
        }
    }
}