package com.example.remarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ReportDto(
    @Json(name = "id")        val id: String,
    @Json(name = "productId") val productId: String,
    @Json(name = "reporterId")val reporterId: String,
    @Json(name = "reason")    val reason: String,
    @Json(name = "active")    val active: Boolean,
    @Json(name = "createdAt") val createdAt: String
)

fun ReportDto.toDomain() = Report(
    id, productId, reporterId, reason, active, createdAt
)
