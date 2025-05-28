package com.example.remarket.data.repository

/*import com.example.remarket.data.model.Product
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.local.FirestoreDataSource
import javax.inject.Inject

/**
 * Repositorio encargado de gestionar las operaciones sobre productos,
 * delegando en la API remota y, opcionalmente, en la fuente local.
 */
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val firestoreDataSource: FirestoreDataSource
){
    /**
     * Crea una nueva publicación de producto en la API remota.
     *
     * @param brand Marca del dispositivo.
     * @param model Modelo del dispositivo.
     * @param storage Capacidad de almacenamiento.
     * @param price Precio del dispositivo.
     */
    suspend fun createProduct(
        brand: String,
        model: String,
        storage: String,
        price: Double
    ) {
        // Construye el cuerpo de la petición
        val request = mapOf(
            "brand" to brand,
            "model" to model,
            "storage" to storage,
            "price" to price
        )
        // Llamada al endpoint REST para crear el producto
        apiService.createProduct(request)
        // (Opcional) Actualizar la caché local
        // firestoreDataSource.addProduct(Product(...))
    }
}*/