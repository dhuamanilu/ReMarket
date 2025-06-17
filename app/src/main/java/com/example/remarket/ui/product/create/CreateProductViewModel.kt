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

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val createProductUseCase: CreateProductUseCase,
    private val cloudinaryService: CloudinaryService  // Inyectamos servicio de Cloudinary
) : ViewModel() {

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

    private val _state = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val state: StateFlow<Resource<Unit>> = _state.asStateFlow()

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

    /**
     * Valida campos individualmente y devuelve mensaje específico.
     */
    private fun validate(): String? {
        return when {
            brand.value.isBlank() -> "Por favor ingresa la marca."
            model.value.isBlank() -> "Por favor ingresa el modelo."
            storage.value.isBlank() -> "Por favor ingresa el almacenamiento."
            price.value <= 0.0 -> "Por favor ingresa un precio válido."
            imei.value.isBlank() -> "Por favor ingresa el IMEI."
            else -> null
        }
    }
    fun submit(context: Context, onSuccess: () -> Unit) {
        // Validación individual
        validate()?.let { msg ->
            _state.value = Resource.Error(msg)
            return
        }


        viewModelScope.launch {
            _state.value = Resource.Loading
            Log.d("CreateProductVM", "Inicio de submit()")
            try {
                Log.d("CreateProductVM", "Subiendo imágenes del producto")
                // 1) Sube fotos del producto
                val uploadedImages = _images.value.map { localUri ->
                    Log.d("CreateProductVM", "Subiendo imagen: $localUri")
                    cloudinaryService.uploadImage(context,localUri)
                }

                // 2) Sube foto de la caja (si existe)
                val boxUrl = _boxImageUrl.value.takeIf { it.isNotBlank() }
                    ?.let {
                        Log.d("CreateProductVM", "Subiendo caja: $it")
                        cloudinaryService.uploadImage(context,it)
                    }

                Log.d("CreateProductVM", "Subiendo imagen de factura")
                // 3) Sube foto de la factura (si existe)
                val invoiceUrl = _invoiceUrl.value.takeIf { it.isNotBlank() }
                    ?.let { Log.d("CreateProductVM", "Subiendo factura: $it")
                        cloudinaryService.uploadImage(context,it)
                    }

                Log.d("CreateProductVM", "Construyendo ProductRequest")

                // 4) Reconstruye el request con las URLs en la nube
                val req = ProductRequest(
                    brand = _brand.value,
                    model = _model.value,
                    storage = _storage.value,
                    price = _price.value,
                    imei = _imei.value,
                    description = _description.value,
                    imageUrls = uploadedImages,
                    boxImageUrl = boxUrl,
                    invoiceUrl = invoiceUrl
                )
                Log.d("CreateProductVM", "Llamando a createProductUseCase")
                // 5) Envía al backend
                when (val result = createProductUseCase(req)) {
                    is Resource.Success -> {
                        _state.value = Resource.Success(Unit)
                        onSuccess()
                    }
                    is Resource.Error -> _state.value = Resource.Error(result.message)
                    Resource.Idle -> TODO()
                    Resource.Loading -> TODO()
                }
            } catch(e: Exception) {
                _state.value = Resource.Error("Error subiendo imágenes: ${e.message}")
            }
        }
    }
}