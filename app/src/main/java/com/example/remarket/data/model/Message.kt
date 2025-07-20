package com.example.remarket.data.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Message(
    @DocumentId // <-- Anotación para que Firestore asigne el ID del documento aquí
    val id: String = "", // <-- AÑADE ESTA LÍNEA
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)