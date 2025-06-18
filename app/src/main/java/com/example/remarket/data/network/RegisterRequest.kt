// File: app/src/main/java/com/example/remarket/data/network/RegisterRequest.kt
package com.example.remarket.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "firstName") val firstName: String,
    @Json(name = "lastName") val lastName: String,
    @Json(name = "dniNumber") val dniNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "dniFrontUrl") val dniFrontUrl: String? = null, // Opcional
    @Json(name = "dniBackUrl") val dniBackUrl: String? = null    // Opcional
)