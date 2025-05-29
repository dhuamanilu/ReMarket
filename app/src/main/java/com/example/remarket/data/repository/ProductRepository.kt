// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay

class ProductRepository {

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
}
