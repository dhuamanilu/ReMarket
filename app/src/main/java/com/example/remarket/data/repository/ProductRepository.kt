// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import javax.inject.Inject // Importar Inject

class ProductRepository @Inject constructor(){

    // Lista de productos “mock” adaptada al nuevo modelo
    private val mockProducts = listOf(
        Product(
            id = "1",
            brand = "Samsung",
            model = "S24 Ultra",
            storage = "256 GB",
            price = 3400.0,
            imageUrl = "https://example.com/images/s24ultra.jpg" // URL de ejemplo
        ),
        Product(
            id = "2",
            brand = "Apple",
            model = "iPhone 13 Pro",
            storage = "128 GB",
            price = 2800.0,
            imageUrl = "https://example.com/images/iphone13pro.jpg"
        )
        // Puedes agregar más productos de prueba aquí…
    )

    /**
     * Devuelve un Flow que emite el producto con el id indicado (o null si no existe).
     * Simula una demora de 1 segundo.
     */
    suspend fun getProductById(productId: String): Flow<Product?> = flow {
        delay(1000)
        val product = mockProducts.find { it.id == productId }
        emit(product)
    }

    /**
     * Simula alternar favorito para el producto dado.
     * Siempre retorna true (en un caso real aquí iría la lógica para marcar/desmarcar favorito).
     */
    suspend fun toggleFavorite(productId: String): Flow<Boolean> = flow {
        delay(500)
        emit(true)
    }

    /**
     * Simula enviar un reporte por un producto (razón cualquiera).
     * Siempre retorna true (en un caso real se capturaría el motivo y se enviaría al servidor).
     */
    suspend fun reportProduct(productId: String, reason: String): Flow<Boolean> = flow {
        delay(500)
        emit(true)
    }

    suspend fun getAllProducts(): Flow<List<Product>> = flow {
        // Simular carga de datos
        delay(1000)
        emit(mockProducts)
    }
    // --- AÑADE ESTA FUNCIÓN ---
    /**
     * Simula la creación de un nuevo producto.
     * En una aplicación real, aquí se haría una llamada a la API o a la base de datos.
     * @throws Exception si la creación falla (simulado).
     */
    suspend fun createProduct(brand: String, model: String, storage: String, price: Double) {
        // Simular una operación de red o base de datos que toma tiempo
        delay(1500)

        // En un caso real, podrías añadir el producto a una lista mutable
        // o verificar si la operación en el servidor fue exitosa.
        println("Producto creado (simulado): $brand $model, Almacenamiento: $storage, Precio: S/ $price")

        // Aquí podrías lanzar una excepción para probar el manejo de errores
        // if (brand.equals("error", ignoreCase = true)) {
        //     throw Exception("Fallo simulado al crear el producto")
        // }
    }
}
