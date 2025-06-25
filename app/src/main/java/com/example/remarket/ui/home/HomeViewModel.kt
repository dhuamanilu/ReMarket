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
        fetchToken() // Asegurarse de que el token exista al iniciar
        observeProducts() // Empezar a escuchar la base de datos local
        onRefresh() // Realizar la primera sincronización de datos
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
                                // Aplicar filtro de búsqueda actual a la nueva lista
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
            _uiState.update { it.copy(isLoading = true) }

            // Llama al repositorio para sincronizar y captura el resultado
            val syncSuccess = productRepository.syncProducts()

            // Si la sincronización falló, debemos detener el indicador de carga manualmente.
            // Si tuvo éxito, el observador de la base de datos se encargará de ello.
            if (!syncSuccess) {
                _uiState.update { it.copy(isLoading = false, error = "Fallo la sincronización. Verifica tu conexión.") }
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