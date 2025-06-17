package com.example.remarket.domain.usecase

import com.example.remarket.data.model.Product
import com.example.remarket.data.repository.IProductRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val repo: IProductRepository
) {
    suspend operator fun invoke(): Flow<List<Product>> = repo.getAllProducts()
}
