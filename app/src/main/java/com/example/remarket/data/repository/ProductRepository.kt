// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import com.example.remarket.data.model.ProductDto
import com.example.remarket.data.network.ApiService
import com.example.remarket.util.Resource
import com.google.gson.JsonParseException
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
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.network.ReportRequest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn

class ProductRepository @Inject constructor(
    private val api: ApiService
) : IProductRepository{
    override suspend fun getAllProducts(): Flow<List<Product>> = flow {
        val dtos = api.getProducts()
        emit(dtos.map { it.toDomain() })
    }
    override suspend fun createProduct(request: ProductRequest): Resource<Product> =
        withContext(Dispatchers.IO) {
            try {
                val dto = api.createProduct(request)
                Resource.Success(dto.toDomain())
            } catch (e: IOException) {
                Resource.Error("Error de red: ${e.localizedMessage}")
            } catch (e: HttpException) {
                Resource.Error("HTTP error: ${e.code()}")
            }
        }
    override suspend fun getProductById(productId: String): Flow<Resource<Product>> = flow {
        try {
            // Llamada al endpoint específico
            val dto= api.getProductById(productId)
            emit(Resource.Success(dto.toDomain()))
        } catch (e: HttpException) {
            // Manejo de errores HTTP (404, 500, etc.)
            val msg = when (e.code()) {
                404 -> "Producto no encontrado (404)"
                500 -> "Error interno del servidor (500)"
                else -> "Error ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(msg))
        } catch (e: IOException) {
            // Errores de red
            emit(Resource.Error("Error de red: ${e.localizedMessage}"))
        }
    }
        .catch { e ->
            // Captura cualquier otra excepción y emite Resource.Error
            emit(Resource.Error(e.localizedMessage ?: "Error desconocido al obtener producto"))
        }
        .flowOn(Dispatchers.IO)

    override suspend fun toggleFavorite(productId: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun reportProduct(productId: String, reason: String): Flow<Boolean> = flow {
        try {
            // Realiza POST /reports
            val response = api.createReport(ReportRequest(productId, reason))
            emit(response.isSuccessful)
        } catch (e: HttpException) {
            emit(false)
        } catch (e: IOException) {
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
}
