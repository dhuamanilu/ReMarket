package com.example.remarket.data.repository

import com.example.remarket.data.model.UserDto
import com.example.remarket.data.network.ApiService
import com.example.remarket.util.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val api: ApiService) {

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
    suspend fun getUserById(userId: String): Resource<UserDto> = withContext(Dispatchers.IO) {
        try {
            val user = api.getUserById(userId)
            Resource.Success(user)
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado")
        } catch (e: IOException) {
            Resource.Error("Error de red: ${e.localizedMessage}")
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                404 -> "Usuario no encontrado (404)"
                500 -> "Error interno del servidor (500)"
                else -> "Error ${e.code()}: ${e.message()}"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error desconocido")
        }
    }
}