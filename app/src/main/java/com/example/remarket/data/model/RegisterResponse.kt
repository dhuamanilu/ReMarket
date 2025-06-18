// File: app/src/main/java/com/example/remarket/data/model/RegisterResponse.kt
package com.example.remarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "message") val message: String,
    @Json(name = "user") val user: UserDto
)