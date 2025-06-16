package com.example.remarket.domain.usecase
import com.example.remarket.data.repository.ProductRepository
import com.example.remarket.util.Resource
import com.tuempresa.remarket.data.network.ProductRequest
import com.tuempresa.remarket.data.network.ProductResponse
import javax.inject.Inject

class CreateProductUseCase @Inject constructor(
    private val repository: ProductRepository
) {
    suspend operator fun invoke(request: ProductRequest): Resource<ProductResponse> {
        return repository.createProduct(request)
    }
}