// File: app/src/main/java/com/example/remarket/data/repository/UserRepository.kt
package com.example.remarket.data.repository

import android.util.Log
import com.example.remarket.data.model.User
import com.example.remarket.data.model.UserDto
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.network.ApproveRequest
import com.example.remarket.data.network.RegisterRequest
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

    /**
     * Crea un nuevo usuario llamando al endpoint de la API.
     * @return Resource<User> que encapsula el resultado.
     */
    suspend fun createUser(
        firstName: String,
        lastName: String,
        dni: String,
        phone: String,
        email: String,
        password: String,
        dniFrontUrl: String?,
        dniBackUrl: String?
    ): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterRequest(
                firstName = firstName,
                lastName = lastName,
                dniNumber = dni,
                email = email,
                password = password,
                dniFrontUrl = dniFrontUrl,
                dniBackUrl = dniBackUrl
            )
            val response = api.registerUser(request)
            Resource.Success(response.user.toDomain())
        } catch (e: HttpException) {
            // Manejar errores HTTP específicos
            val msg = when (e.code()) {
                409 -> "El correo electrónico ya está en uso."
                400 -> "Datos inválidos. Revisa la información."
                else -> "Error ${e.code()}: ${e.message()}"
            }
            Resource.Error(msg)
        } catch (e: IOException) {
            Resource.Error("Error de red. Revisa tu conexión a internet.")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Ocurrió un error inesperado.")
        }
    }

    suspend fun getUserById(userId: String): Resource<UserDto> = withContext(Dispatchers.IO) {
        try {
            val user = api.getUserById(userId)
            Log.d("API_DEBUG", "getUserById → $user")   // ⬅️ imprime el DTO completo
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
    suspend fun getMyProfile(): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val user = api.getMyProfile().toDomain()
            Resource.Success(user)
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado")
        } catch (e: IOException) {
            Resource.Error("Error de red: ${e.localizedMessage}")
        } catch (e: HttpException) {
            val msg = "Error ${e.code()}: ${e.message()}"
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error desconocido")
        }
    }
    //  ➜  Pega esto dentro de la clase UserRepository
//--------------------------------------------------------------------------

    /**  Lista de usuarios NO aprobados (solo admin) */
    suspend fun getPendingUsers(): Resource<List<User>> = withContext(Dispatchers.IO) {
        try {
            val list = api.getPendingUsers().filter { !it.approved }            // <-- solo NO aprobados
                .map { it.toDomain() }
            Resource.Success(list)

        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado")
        } catch (e: IOException) {
            Resource.Error("Error de red: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Resource.Error("Error ${e.code()}: ${e.message()}")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Error desconocido")
        }
    }

    /**  Cambia el campo 'approved' de un usuario  */
    suspend fun setUserApproved(id: String, approved: Boolean): Resource<User> =
        withContext(Dispatchers.IO) {
            try {
                val dto = api.updateUserStatus(id, ApproveRequest(approved))
                Resource.Success(dto.toDomain())
            } catch (e: HttpException) {
                Resource.Error("Error ${e.code()}: ${e.message()}")
            } catch (e: IOException) {
                Resource.Error("Error de red")
            } catch (e: Exception) {
                Resource.Error(e.localizedMessage ?: "Error desconocido")
            }
        }
}