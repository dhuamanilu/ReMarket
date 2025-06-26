// data/worker/SyncWorker.kt
package com.example.remarket.data.worker

import android.Manifest // <-- AÑADIR
import android.app.NotificationChannel // <-- AÑADIR
import android.app.NotificationManager // <-- AÑADIR
import android.content.pm.PackageManager // <-- AÑADIR
import androidx.core.app.ActivityCompat // <-- AÑADIR
import androidx.core.app.NotificationCompat // <-- AÑADIR
import androidx.core.app.NotificationManagerCompat // <-- AÑADIR
import com.example.remarket.R

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.IProductRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted private val appContext: Context, // <-- Hacerlo private val
    @Assisted workerParams: WorkerParameters,
    private val productRepository: IProductRepository, // Inyecta el repositorio
    private val firebaseAuth: FirebaseAuth,
    private val tokenManager: TokenManager
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "ProductSyncWorker"
        private const val CHANNEL_ID = "SYNC_FAILURE_CHANNEL" // <-- AÑADIR

    }

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Iniciando trabajo de sincronización...")

        // --- INICIO DE CAMBIOS: LÓGICA DE OBTENCIÓN DE TOKEN ---
        try {
            // Paso 1: Asegurar que tenemos un token de autenticación.
            if (tokenManager.getToken().isNullOrBlank()) {
                Log.d("SyncWorker", "Token no encontrado en TokenManager. Intentando obtener uno nuevo de Firebase.")
                val currentUser = firebaseAuth.currentUser
                if (currentUser == null) {
                    Log.e("SyncWorker", "No hay usuario logueado. No se puede sincronizar.")
                    // Si no hay usuario, es un fallo permanente para este intento.
                    return Result.failure()
                }

                // Obtenemos el token de forma síncrona dentro de la corrutina
                val tokenResult = withContext(Dispatchers.IO) {
                    Tasks.await(currentUser.getIdToken(true))
                }

                val token = tokenResult.token
                if (token.isNullOrBlank()) {
                    Log.e("SyncWorker", "Se obtuvo un token nulo o vacío de Firebase.")
                    // No se pudo obtener token, reintentar más tarde.
                    return Result.retry()
                }

                tokenManager.saveToken(token)
                Log.d("SyncWorker", "Nuevo token obtenido y guardado exitosamente.")
            }
            // --- FIN DE CAMBIOS ---

            // Paso 2: Proceder con la sincronización (lógica existente)
            val success = productRepository.syncOfflineCreations()
            return if (success) {
                Log.d("SyncWorker", "Sincronización completada exitosamente.")
                Result.success()
            } else {
                Log.d("SyncWorker", "Sincronización falló. Se reintentará más tarde.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("SyncWorker", "Error durante la sincronización: ${e.message}", e)
            // Si el error fue de autenticación, podría ser bueno reintentar.
            // Si fue otro error (como el de la URI de antes), podría ser un fallo.
            if (e is retrofit2.HttpException && e.code() == 401) {
                return Result.retry()
            }
            sendSyncFailedNotification(
                "Error de Sincronización",
                "No se pudieron subir tus productos pendientes. Revisa tu conexión."
            )
            return Result.failure()
        }
    }
    // --- FUNCIÓN AÑADIDA PARA ENVIAR NOTIFICACIÓN ---
    private fun sendSyncFailedNotification(title: String, content: String) {
        // 1. Crear el canal de notificación (necesario para Android 8.0+)
        val name = "Notificaciones de Sincronización"
        val descriptionText = "Canal para notificar fallos en la sincronización de datos"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // 2. Construir la notificación
        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplaza con un ícono de tu app
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // 3. Mostrar la notificación
        with(NotificationManagerCompat.from(appContext)) {
            // Se necesita permiso para notificaciones en API 33+
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("SyncWorker", "Permiso de notificaciones no concedido. No se puede mostrar la notificación.")
                return
            }
            notify(System.currentTimeMillis().toInt(), builder.build()) // Usar un ID único
        }
    }
}