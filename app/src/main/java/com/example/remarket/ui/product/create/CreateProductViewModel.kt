// File: app/src/main/java/com/example/remarket/ui/product/create/CreateProductViewModel.kt
package com.example.remarket.ui.product.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.domain.usecase.CreateProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    //private val createProductUseCase: CreateProductUseCase
) : ViewModel() {

    // Campos de texto introducidos por el usuario
    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand

    private val _modelText = MutableStateFlow("")
    val modelText: StateFlow<String> = _modelText

    private val _storageText = MutableStateFlow("")
    val storageText: StateFlow<String> = _storageText

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price

    // Estados de UI
    private val _isPosting = MutableStateFlow(false)
    val isPosting: StateFlow<Boolean> = _isPosting

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Métodos para actualizar los campos
    fun onBrandChanged(text: String) {
        _brand.value = text
        _error.value = null
    }

    fun onModelChanged(text: String) {
        _modelText.value = text
        _error.value = null
    }

    fun onStorageChanged(text: String) {
        _storageText.value = text
        _error.value = null
    }

    fun onPriceChanged(text: String) {
        _price.value = text
        _error.value = null
    }

    /**
     * Envía la solicitud de creación de producto usando el caso de uso.
     * Valida que todos los campos estén llenos y que el precio sea numérico.
     */
    fun submit(onSuccess: () -> Unit) {
        val brandValue = brand.value.trim()
        val modelValue = modelText.value.trim()
        val storageValue = storageText.value.trim()
        val priceValue = price.value.trim().toDoubleOrNull()

        // Validación básica
        if (brandValue.isEmpty() ||
            modelValue.isEmpty() ||
            storageValue.isEmpty() ||
            priceValue == null
        ) {
            _error.value = "Todos los campos son obligatorios y el precio debe ser numérico"
            return
        }

        /*viewModelScope.launch {
            _isPosting.value = true
            _error.value = null
            try {
                createProductUseCase(
                    brand = brandValue,
                    model = modelValue,
                    storage = storageValue,
                    price = priceValue
                )
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al publicar el producto"
            } finally {
                _isPosting.value = false
            }
        }*/
    }
}
