// ui/admin/review/AdminReviewViewModel.kt
package com.example.remarket.ui.admin.review

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminReviewViewModel @Inject constructor(
    private val repo: IProductRepository
) : ViewModel() {

    // ──────────────────── State (idéntico a CreateProduct) ────────────────────
    private val _brand        = MutableStateFlow("")
    private val _model        = MutableStateFlow("")
    private val _storage      = MutableStateFlow("")
    private val _price        = MutableStateFlow(0.0)
    private val _imei         = MutableStateFlow("")
    private val _description  = MutableStateFlow("")
    private val _images       = MutableStateFlow<List<String>>(emptyList())
    private val _boxImageUrl  = MutableStateFlow("")
    private val _invoiceUrl   = MutableStateFlow("")
    private var currentId: String? = null        // 👈 guarda el id cargado
    val brand        : StateFlow<String>       = _brand
    val model        : StateFlow<String>       = _model
    val storage      : StateFlow<String>       = _storage
    val price        : StateFlow<Double>       = _price
    val imei         : StateFlow<String>       = _imei
    val description  : StateFlow<String>       = _description
    val images       : StateFlow<List<String>> = _images
    val boxImageUrl  : StateFlow<String>       = _boxImageUrl
    val invoiceUrl   : StateFlow<String>       = _invoiceUrl

    private val _ui  = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val ui: StateFlow<Resource<Unit>> = _ui

    // Carga un producto y llena los StateFlow
    fun load(productId: String) = viewModelScope.launch {
        currentId = productId                     // 👈  guarda para luego
        _ui.value = Resource.Loading
        Log.d("VM", "load() con id=$productId")
        when (val res = repo.getProductById(productId).first()) {

            is Resource.Success -> {
                Log.d("VM", "resultado=$res")
                res.data?.let { p -> fill(p) }
                _ui.value = Resource.Success(Unit)
            }
            is Resource.Error   -> _ui.value = Resource.Error(res.message)
            else                -> {}
        }

    }
    // ───── NUEVO: aprobar / rechazar ─────
    suspend fun approve() = setStatus("approved")
    suspend fun reject()  = setStatus("rejected")
    /**  ← DEVUELVE true cuando el repo responde Success  */
    private suspend fun setStatus(status: String): Boolean {
        val id = currentId ?: return false
        return when (repo.updateProductStatus(id, status)) {
            is Resource.Success -> true
            is Resource.Error   -> {            // deja el error en ui
                _ui.value = Resource.Error("Error al $status")
                false
            }
            else -> false
        }
    }
    private fun fill(p: Product) {
        _brand.value       = p.brand
        _model.value       = p.model
        _storage.value     = p.storage
        _price.value       = p.price
        _imei.value        = p.imei
        _description.value = p.description
        _images.value      = p.images
        _boxImageUrl.value = p.box
        _invoiceUrl.value  = p.invoiceUri
    }
}