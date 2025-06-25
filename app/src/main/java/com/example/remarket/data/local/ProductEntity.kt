// data/local/ProductEntity.kt
package com.example.remarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val brand: String,
    val model: String,
    val storage: String,
    val price: Double,
    val imei: String,
    val description: String,
    val imageUrls: List<String>, // Room puede manejar listas de primitivos
    val boxImageUrl: String,
    val invoiceUrl: String,
    val status: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String
)

// --- MAPPERS ---

// Convierte de la Entidad de la DB al Modelo de Dominio (para la UI)
fun ProductEntity.toDomain(): Product = Product(
    id = id,
    sellerId = sellerId,
    brand = brand,
    model = model,
    storage = storage,
    price = price,
    imei = imei,
    description = description,
    images = imageUrls,
    box = boxImageUrl,
    invoiceUri = invoiceUrl,
    status = status,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Convierte del DTO de la Red a la Entidad de la DB (para guardar en Room)
fun ProductDto.toEntity(): ProductEntity = ProductEntity(
    id = id,
    sellerId = sellerId,
    brand = brand,
    model = model,
    storage = storage,
    price = price,
    imei = imei,
    description = description,
    imageUrls = imageUrls,
    boxImageUrl = boxImageUrl,
    invoiceUrl = invoiceUrl,
    status = status,
    active = active,
    createdAt = createdAt,
    updatedAt = updatedAt
)