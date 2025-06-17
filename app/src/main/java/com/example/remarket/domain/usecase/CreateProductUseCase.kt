// File: app/src/main/java/com/example/remarket/domain/usecase/CreateProductUseCase.kt
package com.example.remarket.domain.usecase

import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import javax.inject.Inject

class CreateProductUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    suspend operator fun invoke(request: ProductRequest): Resource<Unit> {
        return when (val res = repository.createProduct(request)) {
            is Resource.Success -> Resource.Success(Unit)
            is Resource.Error -> Resource.Error(res.message)
            else -> Resource.Error("Estado desconocido")
        }
    }
}