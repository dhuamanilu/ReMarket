package com.example.remarket.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
// En algún lugar seguro (p. ej. BuildConfig o un object Constants)
object CloudinaryConfig {
    const val CLOUD_NAME = "dg5llpefb"
    const val API_KEY    = "376576317396596"
    const val API_SECRET = "TU_API_SECRET"  // si usas signed uploads
    // si solo usarás unsigned presets, puedes omitir SECRET
}
@Singleton
class CloudinaryService @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        private var initialized = false
    }

    init {
        if (!initialized) {
            val configMap = mapOf(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME,
                "api_key"    to CloudinaryConfig.API_KEY,
                "api_secret" to CloudinaryConfig.API_SECRET    // o elimínalo si usas unsigned
            )
            MediaManager.init(context, configMap)
            Log.d(
                "CloudinaryService",
                "Inicializado manual: cloud=${CloudinaryConfig.CLOUD_NAME}, key=${CloudinaryConfig.API_KEY}"
            )
            initialized = true
        }
    }

    fun uriToFile(context: Context, uri: Uri): File {
        // --- LÓGICA MODIFICADA PARA SER MÁS ROBUSTA ---
        val inputStream: InputStream? = if (uri.scheme == "content") {
            context.contentResolver.openInputStream(uri)
        } else {
            // Asume que es una URI de archivo (file:///)
            uri.path?.let { File(it) }?.let { FileInputStream(it) }
        }

        if (inputStream == null) {
            throw IOException("No se pudo obtener un InputStream de la URI: $uri")
        }

        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }
    suspend fun uploadImage(context: Context, uriString: String): String = suspendCancellableCoroutine { cont ->
        val uri = Uri.parse(uriString)

        val file = try {
            uriToFile(context, uri)
        } catch (e: Exception) {
            cont.resumeWithException(IllegalStateException("No se pudo convertir URI a File: ${e.message}"))
            return@suspendCancellableCoroutine
        }

        println("🟡 Subiendo archivo desde ruta temporal: ${file.absolutePath}")

        MediaManager.get().upload(file.absolutePath).unsigned("imagenes_android")
            .option("folder", "products")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) cont.resume(secureUrl)
                    else cont.resumeWithException(IllegalStateException("No vino secure_url"))
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception("Error Cloudinary: ${error.description}"))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    cont.resumeWithException(Exception("Reintento Cloudinary: ${error.description}"))
                }
            })
            .dispatch()
    }
}

