// data/repository/IProductRepository.kt
package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import com.example.remarket.data.model.ProductDto
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.util.Resource
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    // Este Flow ahora leerá desde la DB local
    fun getAllProducts(): Flow<Resource<List<Product>>>
    // Nueva función para forzar la sincronización con la red
    suspend fun syncProducts(): Boolean
    suspend fun createProduct(request: ProductRequest, imageUris: List<String>, boxImageUri: String?, invoiceUri: String?): Resource<Product>
    suspend fun updateProduct(productId: String, request: ProductRequest, imageUris: List<String>, boxImageUri: String?, invoiceUri: String?): Resource<Product>
    suspend fun deleteProduct(productId: String): Resource<Unit>
    suspend fun getProductById(productId: String): Flow<Resource<Product>>
    suspend fun toggleFavorite(productId: String): Flow<Boolean>
    suspend fun reportProduct(productId: String, reason: String): Flow<Resource<Unit>>
    suspend fun syncOfflineCreations(): Boolean
    fun triggerOfflineSync()
    fun getPendingProducts(): Flow<Resource<List<Product>>>
    suspend fun updateProductStatus(productId: String, newStatus: String): Resource<Unit>
    fun getPendingProductsFromFirebase(): Flow<Resource<List<Product>>>
    suspend fun getProductByIdFromFirebase(id: String): Resource<Product>
    fun getMyProducts(): Flow<Resource<List<Product>>>
    suspend fun purchaseProduct(productId: String): Flow<Resource<Unit>>
    fun getMyPurchases(): Flow<Resource<List<Product>>>
    suspend fun markProductSold(productId: String): Resource<Product>


}
