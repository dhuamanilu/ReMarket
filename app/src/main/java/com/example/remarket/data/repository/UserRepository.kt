package com.example.remarket.data.repository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor() {

    // Simula una base de datos de usuarios en memoria.
    private val users = mutableMapOf(
        "admin@remarket.com" to "admin123",
        "user@remarket.com" to "user123",
        "test@example.com" to "password"
    )

    /**
     * Valida si las credenciales de un usuario son correctas.
     * @return El email del usuario si es válido, o null si no lo es.
     */
    fun validateUser(email: String, password: String): String? {
        return if (users[email] == password) email else null
    }

    /**
     * Crea un nuevo usuario.
     * @return True si el usuario fue creado, false si el email ya existía.
     */
    fun createUser(email: String, password: String): Boolean {
        if (users.containsKey(email)) {
            return false // El usuario ya existe
        }
        users[email] = password
        return true
    }
}