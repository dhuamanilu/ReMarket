// File: app/src/main/java/com/example/remarket/ui/purchase/PurchaseViewModel.kt
package com.example.remarket.ui.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val repo: IProductRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val success: Boolean = false,
        val error: String? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui: StateFlow<UiState> = _ui

    fun purchase(productId: String) = viewModelScope.launch {
        repo.purchaseProduct(productId).collect { res ->
            when (res) {
                is Resource.Loading  -> _ui.update { it.copy(isLoading = true, error = null) }
                is Resource.Success  -> _ui.update { it.copy(isLoading = false, success = true) }
                is Resource.Error    -> _ui.update { it.copy(isLoading = false, error = res.message) }
                Resource.Idle -> TODO()
            }
        }
    }
}
