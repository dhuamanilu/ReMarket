// File: app/src/main/java/com/example/remarket/ui/product/create/CreateProductViewModel.kt
package com.example.remarket.ui.product.create

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.repository.CloudinaryService
import com.example.remarket.domain.usecase.CreateProductUseCase
import com.example.remarket.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.lifecycle.SavedStateHandle // <-- AÑADIDO


@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase,
    // El contexto ya no es necesario en la función submit porque se inyecta aquí
    private val productRepository: IProductRepository, // <-- AÑADIDO para editar
    @ApplicationContext private val context: Context,
    private val savedStateHandle: SavedStateHandle // <-- AÑADIDO
) : ViewModel() {
    private var isEditMode = false
    private var editingProductId: String? = null

    private val _brand = MutableStateFlow("")
    val brand: StateFlow<String> = _brand.asStateFlow()

    private val _model = MutableStateFlow("")
    val model: StateFlow<String> = _model.asStateFlow()

    private val _storage = MutableStateFlow("")
    val storage: StateFlow<String> = _storage.asStateFlow()

    private val _price = MutableStateFlow(0.0)
    val price: StateFlow<Double> = _price.asStateFlow()

    private val _priceText = MutableStateFlow("")            // texto lineal
    val priceText: StateFlow<String> = _priceText.asStateFlow()

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

    // El estado ahora puede ser de un producto o de nada (Unit)
    private val _state = MutableStateFlow<Resource<Product>>(Resource.Idle)
    val state: StateFlow<Resource<Product>> = _state.asStateFlow()

    fun onBrandChanged(value: String) { _brand.value = value }
    fun onModelChanged(value: String) { _model.value = value }
    fun onStorageChanged(value: String) { _storage.value = value }
    fun onPriceChanged(value: Double) { _price.value = value }
    fun onPriceTextChanged(text: String) {
        _priceText.value = text
        text.toDoubleOrNull()?.let { _price.value = it }
    }
    fun onImeiChanged(value: String) { _imei.value = value }
    fun onDescriptionChanged(value: String) { _description.value = value }
    fun addImage(uri: String) { _images.value = _images.value + uri }
    fun setBoxImage(uri: String) { _boxImageUrl.value = uri }
    fun setInvoiceImage(uri: String) { _invoiceUrl.value = uri }
    fun removeImage(uri: String) {
        _images.value = _images.value.toMutableList().also { it.remove(uri) }
    }

    fun clearBoxImage() {
        _boxImageUrl.value = ""
    }

    fun clearInvoiceImage() {
        _invoiceUrl.value = ""
    }
    // --- NUEVA FUNCIÓN PARA CARGAR DATOS EN MODO EDICIÓN ---
    fun loadProductForEdit(productId: String) {
        if (isEditMode) return // Evita recargar si ya está en modo edición
        isEditMode = true
        editingProductId = productId

        viewModelScope.launch {
            _state.value = Resource.Loading
            productRepository.getProductById(productId).collect { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    val product = resource.data
                    _brand.value = product.brand
                    _model.value = product.model
                    _storage.value = product.storage
                    _price.value = product.price
                    _priceText.value = product.price.toString()
                    _imei.value = product.imei
                    _description.value = product.description
                    _images.value = product.images
                    _boxImageUrl.value = product.box
                    _invoiceUrl.value = product.invoiceUri
                    _state.value = Resource.Idle // Resetea el estado
                } else if (resource is Resource.Error) {
                    _state.value = Resource.Error("No se pudo cargar el producto para editar: ${resource.message}")
                }
            }
        }
    }
    private fun validate(): String? {
        return when {
            brand.value.isBlank() -> "Por favor ingresa la marca."
            model.value.isBlank() -> "Por favor ingresa el modelo."
            storage.value.isBlank() -> "Por favor ingresa el almacenamiento."
            price.value <= 0.0 -> "Por favor ingresa un precio válido."
            imei.value.isBlank() -> "Por favor ingresa el IMEI."
            images.value.isEmpty() -> "Debes agregar al menos una foto del producto."
            else -> null
        }
    }

    // --- FUNCIÓN SUBMIT CORREGIDA ---
    fun submit(onSuccess: () -> Unit) {
        validate()?.let { msg ->
            _state.value = Resource.Error(msg)
            return
        }

        viewModelScope.launch {
            _state.value = Resource.Loading

            val request = ProductRequest(
                brand = _brand.value,
                model = _model.value,
                storage = _storage.value,
                price = _price.value,
                imei = _imei.value,
                description = _description.value,
                imageUrls = emptyList(),
                boxImageUrl = null,
                invoiceUrl = null
            )

            // --- LÓGICA MODIFICADA PARA ELEGIR ENTRE CREAR Y ACTUALIZAR ---
            val result = if (isEditMode && editingProductId != null) {
                productRepository.updateProduct(
                    productId = editingProductId!!,
                    request = request,
                    imageUris = _images.value,
                    boxImageUri = _boxImageUrl.value.takeIf { it.isNotBlank() },
                    invoiceUri = _invoiceUrl.value.takeIf { it.isNotBlank() }
                )
            } else {
                createProductUseCase(
                    request = request,
                    imageUris = _images.value,
                    boxImageUri = _boxImageUrl.value.takeIf { it.isNotBlank() },
                    invoiceUri = _invoiceUrl.value.takeIf { it.isNotBlank() }
                )
            }

            _state.value = result

            if (result is Resource.Success) {
                onSuccess()
            }
        }
    }
}