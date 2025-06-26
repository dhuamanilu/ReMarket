// File: app/src/main/java/com/example/remarket/domain/usecase/CreateProductUseCase.kt
package com.example.remarket.domain.usecase

import com.example.remarket.data.model.Product
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.util.Resource
import javax.inject.Inject

class CreateProductUseCase @Inject constructor(
    private val repository: IProductRepository
) {
    // --- FIRMA MODIFICADA ---
    suspend operator fun invoke(
        request: ProductRequest,
        imageUris: List<String>,
        boxImageUri: String?,
        invoiceUri: String?
    ): Resource<Product> { // Devuelve Resource<Product> para ser consistente con el repo
        // --- LLAMADA MODIFICADA ---
        return repository.createProduct(
            request = request,
            imageUris = imageUris,
            boxImageUri = boxImageUri,
            invoiceUri = invoiceUri
        )
    }
}