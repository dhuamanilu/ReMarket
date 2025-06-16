// File: app/src/main/java/com/example/remarket/data/network/RetrofitClient.kt
package com.example.remarket.data.network

import com.example.remarket.data.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
    private const val BASE_URL = "http://161.132.50.99:9364/"

    // Interceptor de logging (opcional, muy útil en desarrollo)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    // El Retrofit “singleton”
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Aquí expones tu ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
