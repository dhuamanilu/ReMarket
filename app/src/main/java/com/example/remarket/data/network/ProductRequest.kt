// File: app/src/main/java/com/tuempresa/remarket/data/network/ProductRequest.kt
package com.tuempresa.remarket.data.network

import com.google.gson.annotations.SerializedName

data class ProductRequest(
    val brand: String,
    val model: String,
    @SerializedName("capacity") val storage: String,
    val price: Double,
    val imei: String,
    @SerializedName("box/cargador") val boxCharger: String,
    val category: String = "smartphone",
    val description: String,
    @SerializedName("urlVideo") val videoUrl: String? = null,
    @SerializedName("invoice/uri") val invoiceUri: String? = null
)
