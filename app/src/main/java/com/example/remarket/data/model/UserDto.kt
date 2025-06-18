// File: app/src/main/java/com/example/remarket/data/model/UserDto.kt
package com.example.remarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "firstName") val firstName: String, // Cambiado de "name"
    @Json(name = "lastName") val lastName: String,
    @Json(name = "dniNumber") val dniNumber: String,
    @Json(name = "email") val email: String,
    @Json(name = "dniFrontUrl") val dniFrontUrl: String?,
    @Json(name = "dniBackUrl") val dniBackUrl: String?,
    @Json(name = "approved") val approved: Boolean,
    @Json(name = "role") val role: String,
    @Json(name = "active") val active: Boolean,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

fun UserDto.toDomain(): User = User(
    id = id,
    firstName = firstName,
    lastName = lastName,
    email = email,
    role = role,
    isApproved = approved
)