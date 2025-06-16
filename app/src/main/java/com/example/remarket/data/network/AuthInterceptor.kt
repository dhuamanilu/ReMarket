// File: app/src/main/java/com/example/remarket/data/network/AuthInterceptor.kt
package com.example.remarket.data.network

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        Log.d("AuthInterceptor", "Request to ${newRequest.url} -- Authorization: ${newRequest.header("Authorization")}")
        return chain.proceed(newRequest)
    }
}
