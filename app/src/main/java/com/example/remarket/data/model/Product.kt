package com.example.remarket.data.model

/**
 * Representa un producto en el mercado de segunda mano.
 *
 * @property id Identificador único del producto.
 * @property brand Marca del dispositivo.
 * @property model Modelo del dispositivo.
 * @property storage Capacidad de almacenamiento (e.g., "128 GB").
 * @property price Precio del dispositivo.
 * @property imageUrl URL de la imagen principal del producto.
 */
data class Product(
    val id: String,
    val brand: String,
    val model: String,
    val storage: String,
    val price: Double,
    val imageUrl: String? = null
)
