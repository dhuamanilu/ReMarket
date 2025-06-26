// ui/home/HomeViewModel.kt
package com.example.remarket.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class HomeUiState(
    val allProducts: List<Product> = emptyList(), // Lista completa desde la DB
    val filteredProducts: List<Product> = emptyList(), // Lista para mostrar en la UI
    val searchQuery: String = "",
    val isLoading: Boolean = false, // Usado para el indicador de SwipeRefresh
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: IProductRepository,
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        fetchToken()
        observeProducts()
        onRefresh()
    }

    private fun observeProducts() {
        viewModelScope.launch {
            productRepository.getAllProducts().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        val products = resource.data
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                allProducts = products,
                                filteredProducts = filter(products, it.searchQuery),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message
                            )
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    /**
     * Inicia una sincronización de datos desde la red.
     * Es llamado al inicio y cuando el usuario desliza para refrescar.
     */
    fun onRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // --- INICIO DE CAMBIOS ---
            // PASO 1: En lugar de ejecutar la sincronización aquí,
            // simplemente le pedimos a WorkManager que lo haga.
            // WorkManager se encargará de no duplicar el trabajo.
            Log.d("HomeViewModel", "onRefresh: Solicitando sincronización de creaciones offline.")
            productRepository.triggerOfflineSync()

            // PASO 2: La responsabilidad de onRefresh ahora es solo
            // traer la lista actualizada de productos desde el servidor.
            val remoteSyncSuccess = productRepository.syncProducts()
            if (!remoteSyncSuccess && _uiState.value.allProducts.isEmpty()) {
                _uiState.update { it.copy(error = "Fallo la sincronización. Verifica tu conexión.") }
            }

            // El isLoading se pondrá en false automáticamente cuando el Flow
            // de `observeProducts` emita el nuevo estado (ya sea con éxito o con error).
            // Si la sincronización remota falla, nos aseguramos de quitar el loading.
            if(!remoteSyncSuccess){
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }


    fun onSearchQueryChanged(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredProducts = filter(currentState.allProducts, query)
            )
        }
    }

    private fun filter(list: List<Product>, q: String): List<Product> =
        if (q.isBlank()) {
            list
        } else {
            list.filter {
                it.brand.contains(q, true) ||
                        it.model.contains(q, true) ||
                        it.storage.contains(q, true)
            }
        }

    fun fetchToken() {
        if (tokenManager.getToken() == null && firebaseAuth.currentUser != null) {
            viewModelScope.launch {
                try {
                    val tokenResult = withContext(Dispatchers.IO) {
                        Tasks.await(firebaseAuth.currentUser!!.getIdToken(true))
                    }
                    tokenManager.saveToken(tokenResult.token ?: "")
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "No se pudo refrescar la sesión.") }
                }
            }
        }
    }

    fun onLogout() {
        firebaseAuth.signOut()
        tokenManager.clearToken()
    }
}