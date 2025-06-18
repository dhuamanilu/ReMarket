// File: app/src/main/java/com/example/remarket/data/network/TokenManager.kt
package com.example.remarket.data.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {
    private var currentToken: String? = null

    fun saveToken(token: String) {
        currentToken = token
    }

    fun getToken(): String? {
        return currentToken
    }

    fun clearToken() {
        currentToken = null
    }
}