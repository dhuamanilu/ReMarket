// File: app/src/main/java/com/example/remarket/data/model/User.kt
package com.example.remarket.data.model

// Descomenta y define la clase User
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val role: String,
    val isApproved: Boolean,
    val dniFrontUrl: String? = null,   // ➊
    val dniBackUrl: String? = null     // ➊
)