// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.local.ProductDao
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
import com.example.remarket.data.model.toEntity
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.network.ReportRequest
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val dao: ProductDao // <-- Inyectar el DAO
) : IProductRepository {

    /**
     * Esta es la "Fuente de Verdad Única".
     * La UI siempre observará este flujo, que emite datos desde la BD local.
     */
    override fun getAllProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        // Emite los datos de la base de datos. map convierte List<ProductEntity> a List<Product>
        val localDataFlow = dao.getProducts().map { entities ->
            entities.map { it.toDomain() }
        }

        localDataFlow.collect { products ->
            emit(Resource.Success(products))
        }
    }

    /**
     * Única función que habla con la red para obtener la lista de productos.
     * Obtiene los datos, limpia la BD local y guarda los nuevos.
     */
    override suspend fun syncProducts(): Boolean {
        return try {
            val remoteProducts = api.getProducts()
            dao.deleteAll()
            dao.insertAll(remoteProducts.map { it.toEntity() })
            true // <-- Devolver true en caso de éxito
        } catch (e: HttpException) {
            println("syncProducts Error HTTP: ${e.message()}")
            false // <-- Devolver false en caso de fallo
        } catch (e: IOException) {
            println("syncProducts Error IO: ${e.message}")
            false // <-- Devolver false en caso de fallo
        }
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
