package com.example.remarket.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductDto(
    @Json(name = "id") val id: String,
    @Json(name = "sellerId") val sellerId: String,
    @Json(name = "brand") val brand: String,
    @Json(name = "model") val model: String,
    @Json(name = "storage") val storage: String,
    @Json(name = "price") val price: Double,
    @Json(name = "imei") val imei: String,
    @Json(name = "description") val description: String,
    @Json(name = "imageUrls") val imageUrls: List<String>,
    @Json(name = "boxImageUrl") val boxImageUrl: String,
    @Json(name = "invoiceUrl") val invoiceUrl: String,
    @Json(name = "status") val status: String,
    @Json(name = "active") val active: Boolean,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)


fun ProductDto.toDomain(): Product = Product(
    id         = id,
    brand      = brand,
    model      = model,
    storage    = storage,
    price      = price,
    description= description,
    box        = boxImageUrl,
    active     = active,
    images     = imageUrls,
    invoiceUri = invoiceUrl,
    createdAt  = createdAt,
    updatedAt  = updatedAt,
    imei       = imei,
    sellerId   = sellerId,
    status     = status
)
