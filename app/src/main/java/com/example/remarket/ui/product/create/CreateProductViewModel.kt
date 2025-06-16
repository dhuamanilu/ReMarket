// File: app/src/main/java/com/example/remarket/ui/product/create/CreateProductViewModel.kt
package com.example.remarket.ui.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.domain.usecase.CreateProductUseCase
import com.example.remarket.util.Resource
import com.tuempresa.remarket.data.network.ProductRequest
import com.tuempresa.remarket.data.network.ProductResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    // (El resto de tus StateFlows se mantienen igual)
    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand

    private val _modelText = MutableStateFlow("")
    val modelText: StateFlow<String> = _modelText

    private val _storageText = MutableStateFlow("")
    val storageText: StateFlow<String> = _storageText

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price

    private val _createState = MutableStateFlow<Resource<ProductResponse>>(Resource.Idle)
    val createState: StateFlow<Resource<ProductResponse>> = _createState
    fun onBrandChanged(text: String) {
        _brand.value = text
    }

    fun onModelChanged(text: String) {
        _modelText.value = text
    }

    fun onStorageChanged(text: String) {
        _storageText.value = text
    }

    fun onPriceChanged(text: String) {
        // Permitir solo números y un punto decimal
        if (text.matches(Regex("^\\d*\\.?\\d*\$"))) {
            _price.value = text
        }
    }

    fun submit(onSuccess: () -> Unit) {
        val request = ProductRequest(
            brand = brand.value.trim(),
            model = modelText.value.trim(),
            storage = storageText.value.trim(),
            price = price.value.trim().toDoubleOrNull() ?: -1.0,
            imei = "123456789012345",     // usa un campo de IMEI real
            boxCharger = "Sí",
            description = "Descripción de prueba"
        )
        // Validación
        if (request.brand.isBlank() || request.model.isBlank() ||
            request.storage.isBlank() || request.price < 0
        ) {
            _createState.value = Resource.Error("Todos los campos son obligatorios y el precio debe ser numérico")
            return
        }

        viewModelScope.launch {
            _createState.value = Resource.Loading
            when (val result = createProductUseCase(request)) {
                is Resource.Success -> {
                    _createState.value = Resource.Success(result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    _createState.value = Resource.Error(result.message)
                }
                is Resource.Loading -> {
                    // Aunque normalmente el use case no volverá a emitir Loading aquí,
                    // lo manejamos para cumplir con la exhaustividad
                    _createState.value = Resource.Loading
                }
                is Resource.Idle-> {
                    // Manejar el estado inicial si es necesario
                    _createState.value = Resource.Idle
                }
            }
        }
    }
}