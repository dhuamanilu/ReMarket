package com.example.remarket.data.model

data class Report(
    val id: String,
    val productId: String,
    val reporterId: String,
    val reason: String,
    val active: Boolean,
    val createdAt: String
)
