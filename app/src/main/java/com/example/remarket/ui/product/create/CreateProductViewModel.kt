// File: app/src/main/java/com/example/remarket/ui/product/create/CreateProductViewModel.kt
package com.example.remarket.ui.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.domain.usecase.CreateProductUseCase
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _model = MutableStateFlow("")
    val model: StateFlow<String> = _model.asStateFlow()

    private val _storage = MutableStateFlow("")
    val storage: StateFlow<String> = _storage.asStateFlow()

    private val _price = MutableStateFlow(0.0)
    val price: StateFlow<Double> = _price.asStateFlow()

    private val _imei = MutableStateFlow("")
    val imei: StateFlow<String> = _imei.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _images = MutableStateFlow<List<String>>(emptyList())
    val images: StateFlow<List<String>> = _images.asStateFlow()

    private val _boxImageUrl = MutableStateFlow("")
    val boxImageUrl: StateFlow<String> = _boxImageUrl.asStateFlow()

    private val _invoiceUrl = MutableStateFlow("")
    val invoiceUrl: StateFlow<String> = _invoiceUrl.asStateFlow()

    private val _state = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val state: StateFlow<Resource<Unit>> = _state.asStateFlow()

    fun onBrandChanged(value: String) { _brand.value = value }
    fun onModelChanged(value: String) { _model.value = value }
    fun onStorageChanged(value: String) { _storage.value = value }
    fun onPriceChanged(value: Double) { _price.value = value }
    fun onImeiChanged(value: String) { _imei.value = value }
    fun onDescriptionChanged(value: String) { _description.value = value }
    fun addImage(uri: String) { _images.value = _images.value + uri }
    fun setBoxImage(uri: String) { _boxImageUrl.value = uri }
    fun setInvoiceImage(uri: String) { _invoiceUrl.value = uri }

    private fun buildRequest(): ProductRequest = ProductRequest(
        brand = _brand.value,
        model = _model.value,
        storage = _storage.value,
        price = _price.value,
        imei = _imei.value,
        description = _description.value,
        imageUrls = _images.value,
        boxImageUrl = _boxImageUrl.value.ifBlank { null },
        invoiceUrl = _invoiceUrl.value.ifBlank { null }
    )

    fun submit(onSuccess: () -> Unit) {
        val req = buildRequest()
        if (req.brand.isBlank() || req.model.isBlank() || req.storage.isBlank() || req.price <= 0.0 || req.imei.isBlank()) {
            _state.value = Resource.Error("Completa todos los campos obligatorios")
            return
        }
        viewModelScope.launch {
            _state.value = Resource.Loading
            when (val result = createProductUseCase(req)) {
                is Resource.Success -> {
                    _state.value = Resource.Success(Unit)
                    onSuccess()
                }
                is Resource.Error -> _state.value = Resource.Error(result.message)
                else -> Unit
            }
        }
    }
}