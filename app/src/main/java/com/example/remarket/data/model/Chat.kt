package com.example.remarket.data.model

import com.google.firebase.firestore.IgnoreExtraProperties // <-- AÑADE ESTE IMPORT

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

@IgnoreExtraProperties
data class Chat(
    val id: String = "",
    val productId: String = "",
    val productTitle: String = "",
    val productImageUrl: String = "",
    val productPrice: Double = 0.0,
    val participantIds: List<String> = emptyList(),
    val sellerId: String = "",
    val buyerId: String = "",
    val lastMessage: String = "",
    @ServerTimestamp
    val lastMessageTimestamp: Date? = null
)