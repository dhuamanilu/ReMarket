// File: app/src/main/java/com/tuempresa/remarket/data/network/ProductResponse.kt
package com.tuempresa.remarket.data.network

import com.google.gson.annotations.SerializedName
import java.time.OffsetDateTime

data class ProductResponse(
    val id: String,
    val brand: String,
    val model: String,
    @SerializedName("capacity") val storage: String,
    val price: Double,
    val imei: String,
    @SerializedName("box/cargador") val boxCharger: String,
    val category: String,
    val description: String,
    @SerializedName("images(s)") val images: List<String>,
    @SerializedName("invoice/uri") val invoiceUri: String?,
    val sellerId: String,
    val status: String,
    val urlVideo: String?,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
)
