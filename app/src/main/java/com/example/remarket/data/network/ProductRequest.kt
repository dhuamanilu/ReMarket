// File: app/src/main/java/com/example/remarket/data/network/ProductRequest.kt
package com.example.remarket.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductRequest(
    val brand: String,
    val model: String,
    val storage: String,
    val price: Double,
    val imei: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val boxImageUrl: String? = null,
    val invoiceUrl: String? = null
)