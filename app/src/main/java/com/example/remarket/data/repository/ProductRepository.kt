// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import com.example.remarket.data.network.ApiService
import com.example.remarket.util.Resource
import com.google.gson.JsonParseException
import com.tuempresa.remarket.data.network.ProductRequest
import com.tuempresa.remarket.data.network.ProductResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject // Importar Inject

class ProductRepository @Inject constructor(
    private val api: ApiService
){

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
    suspend fun createProduct(
        request: ProductRequest
    ): Resource<ProductResponse> {
        return withContext(Dispatchers.IO){
            try {
                val response = api.createPublication(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Resource.Success(body)
                    } else {
                        Resource.Error("Respuesta vacía del servidor")
                    }
                } else {
                    // Mapear códigos HTTP comunes
                    val msg = when (response.code()) {
                        404 -> "Recurso no encontrado (404)"
                        500 -> "Error interno del servidor (500)"
                        else -> "Error ${response.code()}: ${response.message()}"
                    }
                    Resource.Error(msg)
                }
            }
            catch (e: UnknownHostException) {
                // No hay conexión a internet / DNS falla
                Resource.Error("Sin conexión a internet")
            } catch (e: SocketTimeoutException) {
                // El servidor tarda demasiado en responder
                Resource.Error("Tiempo de espera agotado")
            } catch (e: IOException) {
                // Otros errores de E/S
                Resource.Error("Error de red: ${e.localizedMessage}")
            } catch (e: HttpException) {
                // Errores HTTP no exitosos fuera de isSuccessful
                Resource.Error("Error de red: ${e.code()}")
            } catch (e: JsonParseException) {
                // JSON mal formado
                Resource.Error("Respuesta inválida del servidor")
            } catch (e: Exception) {
                // Cualquier otro error
                Resource.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
    }
}
