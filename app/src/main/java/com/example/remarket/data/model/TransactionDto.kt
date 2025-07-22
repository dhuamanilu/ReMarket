// File: app/src/main/java/com/example/remarket/data/model/TransactionDto.kt
package com.example.remarket.data.model

data class TransactionDto(
    val id: String,
    val productId: String,
    val buyerId: String,
    val sellerId: String,
    val status: String,
    val active: Boolean,
    val timestamp: String
)

fun TransactionDto.toDomain() = this
