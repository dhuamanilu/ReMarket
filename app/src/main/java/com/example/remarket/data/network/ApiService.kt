// File: app/src/main/java/com/tuempresa/remarket/data/network/ApiService.kt
package com.example.remarket.data.network

import com.tuempresa.remarket.data.network.ProductRequest
import com.tuempresa.remarket.data.network.ProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("publications")
    suspend fun createPublication(
        @Body request: ProductRequest
    ): Response<ProductResponse>
}
