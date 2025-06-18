// File: app/src/main/java/com/example/remarket/data/repository/ConnectivityRepository.kt
package com.example.remarket.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Interfaz para facilitar las pruebas y la inyección de dependencias
interface IConnectivityRepository {
    fun isNetworkAvailable(): Boolean
}

@Singleton
class ConnectivityRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : IConnectivityRepository {

    override fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }
}