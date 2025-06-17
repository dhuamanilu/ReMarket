package com.example.remarket.data.repository

import com.example.remarket.data.model.Product
import com.example.remarket.data.model.ProductDto
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.util.Resource
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    suspend fun getAllProducts(): Flow<List<Product>>
    suspend fun createProduct(request: ProductRequest): Resource<Product>
    suspend fun getProductById(productId: String): Flow<Resource<Product>>
    suspend fun toggleFavorite(productId: String): Flow<Boolean>
    suspend fun reportProduct(productId: String, reason: String): Flow<Boolean>
}
