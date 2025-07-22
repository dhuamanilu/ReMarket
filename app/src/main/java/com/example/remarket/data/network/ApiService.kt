// File: app/src/main/java/com/tuempresa/remarket/data/network/ApiService.kt
package com.example.remarket.data.network

import android.service.autofill.SaveRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.DELETE // <-- AÑADIDO
import retrofit2.http.PUT // <-- AÑADIDO
import com.example.remarket.data.model.ProductDto
import com.example.remarket.data.model.UserDto
import com.example.remarket.data.model.RegisterResponse
import com.example.remarket.data.model.Chat // <-- AÑADE ESTE IMPORT
import com.example.remarket.data.model.ReportDto
import com.example.remarket.data.model.TransactionDto
import com.example.remarket.data.network.StartChatRequest // <-- AÑADE ESTE IMPORT
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    @POST("products")
    suspend fun createProduct(@Body request: ProductRequest): ProductDto
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/mine")
    suspend fun getMyProducts(): List<ProductDto>

    @GET("products/{productId}")
    suspend fun getProductById(
        @Path("productId") productId: String
    ): ProductDto

    @POST("products/{productId}/purchase")
    suspend fun markProductSold(
        @Path("productId") productId: String
    ): ProductDto

    @GET("reports")
    suspend fun getReports(): List<ReportDto>

    @GET("reports/{reportId}")
    suspend fun getReportById(@Path("reportId") reportId: String): ReportDto

    @DELETE("reports/{reportId}")
    suspend fun deleteReport(@Path("reportId") reportId: String): Response<Unit>

    @POST("saved")
    suspend fun saveProduct(@Body request: SaveRequest): Response<Unit>

    @POST("reports")
    suspend fun createReport(@Body request: ReportRequest): Response<Unit>

    @PUT("products/{productId}") // <-- AÑADIDO
    suspend fun updateProduct(
        @Path("productId") productId: String,
        @Body request: ProductRequest // Reutilizamos el ProductRequest
    ): ProductDto

    @DELETE("products/{productId}") // <-- AÑADIDO
    suspend fun deleteProduct(@Path("productId") productId: String): Response<Unit>

    @GET("users/{userId}")
    suspend fun getUserById(
        @Path("userId") userId: String
    ): UserDto
    @GET("users/me")
    suspend fun getMyProfile(): UserDto

    @POST("chats") // <-- AÑADE ESTA NUEVA FUNCIÓN
    suspend fun startOrGetChat(@Body request: StartChatRequest): Chat

    // Solo admin: usuarios sin aprobar
    @GET("users?approved=false")
    suspend fun getPendingUsers(): List<UserDto>

    @PUT("users/{userId}")
    suspend fun updateUserStatus(
        @Path("userId") userId: String,
        @Body request: ApproveRequest
    ): UserDto
    // Lista “Mis compras” (status reserved | sold)  ➌
    @GET("products/my-purchases")
    suspend fun getMyPurchases(): List<ProductDto>

    // Inicia transacción (reserva) – comprador  ➍
    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionDto
}
