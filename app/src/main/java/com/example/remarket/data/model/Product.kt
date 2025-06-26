package com.example.remarket.data.model

data class Product(
    val id: String,
    val sellerId: String,
    val brand: String,
    val model: String,
    val storage: String,
    val price: Double,
    val imei: String,
    val description: String,
    val images: List<String>,
    val box: String,
    val invoiceUri: String,
    val status: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Boolean = true

)
