package com.example.remarket.data.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StartChatRequest(
    val productId: String
)