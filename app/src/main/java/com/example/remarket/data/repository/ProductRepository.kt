// data/repository/ProductRepository.kt
package com.example.remarket.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.remarket.data.local.ProductDao
import com.example.remarket.data.model.Product
import com.example.remarket.data.model.ProductDto
import com.example.remarket.data.model.ProductEntity
import com.example.remarket.data.network.ApiService
import com.example.remarket.util.Resource
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject // Importar Inject
import com.example.remarket.data.model.toDomain
import com.example.remarket.data.model.toEntity
import com.example.remarket.data.network.ProductRequest
import com.example.remarket.data.network.ReportRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import com.example.remarket.data.worker.SyncWorker
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.ZonedDateTime
import java.util.UUID
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProductRepository @Inject constructor(
    private val api: ApiService,
    private val dao: ProductDao,
    private val cloudinaryService: CloudinaryService,
    private val connectivityRepository: IConnectivityRepository,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore // <-- Añadir esta línea
) : IProductRepository {

    /**
     * Esta es la "Fuente de Verdad Única".
     * La UI siempre observará este flujo, que emite datos desde la BD local.
     */
    override fun getAllProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        // Emite los datos de la base de datos. map convierte List<ProductEntity> a List<Product>
        val localDataFlow = dao.getProducts().map { entities ->
            entities.map { it.toDomain() }
        }

        localDataFlow.collect { products ->
            emit(Resource.Success(products))
        }
    }

    /**
     * Única función que habla con la red para obtener la lista de productos.
     * Obtiene los datos, limpia la BD local DE PRODUCTOS SINCRONIZADOS y guarda los nuevos.
     */
    override suspend fun syncProducts(): Boolean {
        // --- LÓGICA MODIFICADA ---
        return try {
            // 1. Obtiene los productos frescos de la red.
            val remoteProducts = api.getProducts()

            // 2. Borra únicamente los productos que estaban previamente sincronizados.
            //    Los productos creados offline (isSynced = false) permanecerán en la BD.
            dao.deleteSynced()

            // 3. Inserta los nuevos datos. La estrategia OnConflict.REPLACE se encargará
            //    de actualizar cualquier producto que pudiera haber cambiado.
            dao.insertAll(remoteProducts.map { it.toEntity() })

            true // <-- Devolver true en caso de éxito
        } catch (e: HttpException) {
            println("syncProducts Error HTTP: ${e.message()}")
            false // <-- Devolver false en caso de fallo
        } catch (e: IOException) {
            println("syncProducts Error IO: ${e.message}")
            false // <-- Devolver false en caso de fallo
        }
    }

    /**
     * Copia el contenido de una URI (content:// o file://) a un archivo
     * en el almacenamiento interno de la app para asegurar acceso persistente.
     */
    private fun copyUriToInternalStorage(uriString: String, childFolder: String): String? {
        if (uriString.isBlank()) return null
        return try {
            val inputStream: InputStream = context.contentResolver.openInputStream(Uri.parse(uriString)) ?: return null

            // Crea un directorio para las imágenes offline si no existe
            val offlineImagesDir = File(context.filesDir, childFolder)
            if (!offlineImagesDir.exists()) {
                offlineImagesDir.mkdirs()
            }

            // Crea un archivo único en el directorio
            val file = File(offlineImagesDir, "offline_img_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            // Copia los datos
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            // Devuelve la URI del archivo copiado, que es persistente
            Uri.fromFile(file).toString()
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al copiar URI a almacenamiento interno", e)
            null // Devuelve null si falla la copia
        }
    }

    override suspend fun createProduct(
        request: ProductRequest,
        imageUris: List<String>,
        boxImageUri: String?,
        invoiceUri: String?
    ): Resource<Product> = withContext(Dispatchers.IO) {
        if (connectivityRepository.isNetworkAvailable()) {
            // --- Flujo Online (sin cambios) ---
            try {
                val uploadedImageUrls = imageUris.map { cloudinaryService.uploadImage(context, it) }
                val uploadedBoxUrl = boxImageUri?.let { cloudinaryService.uploadImage(context, it) }
                val uploadedInvoiceUrl = invoiceUri?.let { cloudinaryService.uploadImage(context, it) }

                val finalRequest = request.copy(
                    imageUrls = uploadedImageUrls,
                    boxImageUrl = uploadedBoxUrl,
                    invoiceUrl = uploadedInvoiceUrl
                )

                val productDto = api.createProduct(finalRequest)
                dao.insert(productDto.toEntity())
                Resource.Success(productDto.toDomain())
            } catch (e: Exception) {
                Resource.Error("Error al crear el producto online: ${e.message}")
            }
        } else {
            // --- Flujo Offline (LÍNEAS MODIFICADAS) ---
            val temporaryId = "offline_${UUID.randomUUID()}"
            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId == null) {
                // Si no hay usuario, no se puede crear el producto offline.
                // Esto es un caso extremo pero necesario de manejar.
                return@withContext Resource.Error("No se pudo identificar al usuario para la creación offline.")
            }
            val localImagePaths = imageUris.mapNotNull { copyUriToInternalStorage(it, "product_images") }
            val localBoxImagePath = boxImageUri?.let { copyUriToInternalStorage(it, "box_images") } ?: ""
            val localInvoicePath = invoiceUri?.let { copyUriToInternalStorage(it, "invoice_images") } ?: ""

            // 2. Verifica si las imágenes principales se pudieron copiar
            if (localImagePaths.size != imageUris.size) {
                return@withContext Resource.Error("Error al guardar las imágenes para el modo offline. Intenta de nuevo.")
            }

            val newProductEntity = ProductEntity(
                id = temporaryId,
                sellerId = currentUserId, // <-- LÍNEA MODIFICADA
                brand = request.brand,
                model = request.model,
                storage = request.storage,
                price = request.price,
                imei = request.imei,
                description = request.description,
                imageUrls = localImagePaths, // Guardamos URIs locales temporalmente
                boxImageUrl = localBoxImagePath?: "",
                invoiceUrl = localInvoicePath,
                status = "pending_sync",
                active = true,
                createdAt = ZonedDateTime.now().toString(),
                updatedAt = ZonedDateTime.now().toString(),
                isSynced = false // <-- MARCADO PARA SINCRONIZACIÓN
            )
            dao.insert(newProductEntity)
            triggerOfflineSync() // Agenda el trabajo de sincronización
            Resource.Success(newProductEntity.toDomain())
        }
    }

    // --- FUNCIÓN 'updateProduct' CORREGIDA Y ROBUSTA ---
    override suspend fun updateProduct(
        productId: String,
        request: ProductRequest,
        imageUris: List<String>,
        boxImageUri: String?,
        invoiceUri: String?
    ): Resource<Product> = withContext(Dispatchers.IO) {

        // La edición sí requerirá conexión para evitar conflictos de sincronización.
        if (!connectivityRepository.isNetworkAvailable()) {
            return@withContext Resource.Error("Se necesita conexión a internet para actualizar el producto.")
        }

        try {
            // Sube solo las imágenes que NO son de Cloudinary (las nuevas que el usuario seleccionó)
            val uploadedImageUrls = imageUris.map { uri ->
                if (uri.startsWith("http")) {
                    uri // Es una URL de Cloudinary ya existente, no la vuelvas a subir.
                } else {
                    cloudinaryService.uploadImage(context, uri) // Es una URI local nueva, súbela.
                }
            }

            // Lo mismo para la caja y la factura
            val finalBoxUrl = boxImageUri?.let { uri ->
                if (uri.startsWith("http")) uri else cloudinaryService.uploadImage(context, uri)
            }
            val finalInvoiceUrl = invoiceUri?.let { uri ->
                if (uri.startsWith("http")) uri else cloudinaryService.uploadImage(context, uri)
            }

            // Crea la petición final con las URLs actualizadas
            val finalRequest = request.copy(
                imageUrls = uploadedImageUrls,
                boxImageUrl = finalBoxUrl,
                invoiceUrl = finalInvoiceUrl
            )

            // Llama a la API para actualizar el producto en el backend [cite: 58]
            val updatedProductDto = api.updateProduct(productId, finalRequest)

            // Actualiza la base de datos local con la nueva información del servidor
            dao.insert(updatedProductDto.toEntity()) // OnConflictStrategy.REPLACE lo actualizará

            Resource.Success(updatedProductDto.toDomain())

        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al actualizar producto", e)
            Resource.Error("Error al actualizar: ${e.message}")
        }
    }

    // --- FUNCIÓN AÑADIDA PARA ELIMINAR ---
    override suspend fun deleteProduct(productId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        if (!connectivityRepository.isNetworkAvailable()) {
            return@withContext Resource.Error("Se necesita conexión para eliminar el producto.")
        }
        try {
            val response = api.deleteProduct(productId)
            if (response.isSuccessful) {
                // Borra también de la base de datos local
                dao.deleteById(productId)
                Resource.Success(Unit)
            } else {
                Resource.Error("Error al eliminar el producto (código: ${response.code()})")
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "Error al eliminar producto", e)
            Resource.Error("Error de red al eliminar el producto: ${e.message}")
        }
    }


    override suspend fun getProductById(productId: String): Flow<Resource<Product>> = flow {
        Log.d("Repo", "   >>> LLAMANDO API CON ID = $productId")
        try {
            // Llamada al endpoint específico
            val dto= api.getProductById(productId)
            emit(Resource.Success(dto.toDomain()))
        } catch (e: HttpException) {
            // Manejo de errores HTTP (404, 500, etc.)
            val msg = when (e.code()) {
                404 -> "Producto no encontrado (404)"
                500 -> "Error interno del servidor (500)"
                else -> "Error ${e.code()}: ${e.message()}"
            }
            emit(Resource.Error(msg))
        } catch (e: IOException) {
            // Errores de red
            emit(Resource.Error("Error de red: ${e.localizedMessage}"))
        }
    }
        .catch { e ->
            // Captura cualquier otra excepción y emite Resource.Error
            emit(Resource.Error(e.localizedMessage ?: "Error desconocido al obtener producto"))
        }
        .flowOn(Dispatchers.IO)

    override suspend fun toggleFavorite(productId: String): Flow<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun reportProduct(productId: String, reason: String): Flow<Boolean> = flow {
        try {
            // Realiza POST /reports
            val response = api.createReport(ReportRequest(productId, reason))
            emit(response.isSuccessful)
        } catch (e: HttpException) {
            emit(false)
        } catch (e: IOException) {
            emit(false)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncOfflineCreations(): Boolean = withContext(Dispatchers.IO) {
        val unsyncedProducts = dao.getUnsyncedProducts()
        if (unsyncedProducts.isEmpty()) {
            Log.d("ProductRepository", "syncOfflineCreations: No hay productos para sincronizar.") // <-- LOG 1
            return@withContext true
        }
        Log.d("ProductRepository", "syncOfflineCreations: Encontrados ${unsyncedProducts.size} productos para sincronizar.") // <-- LOG 2

        var allSucceeded = true
        for (product in unsyncedProducts) {
            try {
                Log.d("ProductRepository", "syncOfflineCreations: Sincronizando producto con ID local ${product.id}") // <-- LOG 3

                // 1. Sube las imágenes locales a Cloudinary
                val uploadedImageUrls = product.imageUrls.map { cloudinaryService.uploadImage(context, it) }
                val boxUrl = if (product.boxImageUrl.isNotBlank()) cloudinaryService.uploadImage(context, product.boxImageUrl) else null
                val invoiceUrl = if (product.invoiceUrl.isNotBlank()) cloudinaryService.uploadImage(context, product.invoiceUrl) else null

                // 2. Crea el request para la API
                val request = ProductRequest(
                    brand = product.brand,
                    model = product.model,
                    storage = product.storage,
                    price = product.price,
                    imei = product.imei,
                    description = product.description,
                    imageUrls = uploadedImageUrls,
                    boxImageUrl = boxUrl,
                    invoiceUrl = invoiceUrl
                )

                // 3. Llama a la API para crear el producto
                val syncedDto = api.createProduct(request)

                // 4. Si tiene éxito, borra el producto local temporal
                dao.deleteById(product.id)

                // 5. Inserta el producto real del servidor
                dao.insert(syncedDto.toEntity())
                Log.d("ProductRepository", "syncOfflineCreations: Producto ${product.id} sincronizado exitosamente.") // <-- LOG 4
                (product.imageUrls + product.boxImageUrl + product.invoiceUrl).forEach { uriString ->
                    if (uriString.isNotBlank()) {
                        try {
                            val file = File(Uri.parse(uriString).path!!)
                            if (file.exists()) {
                                file.delete()
                            }
                        } catch (e: Exception) {
                            Log.e("ProductRepository", "Error al borrar archivo local: $uriString", e)
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("ProductRepository", "syncOfflineCreations: Falló la sincronización para el producto ${product.id}", e) // <-- LOG 5
                allSucceeded = false
                continue // Continúa con el siguiente producto
            }
        }
        return@withContext allSucceeded
    }

    override fun triggerOfflineSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            // --- INICIO DE LA CORRECCIÓN ---
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, // Tiempo de espera inicial antes de reintentar
                TimeUnit.SECONDS // Unidad de tiempo
            )
            // --- FIN DE LA CORRECCIÓN ---
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
        Log.d("ProductRepository", "Se solicitó un trabajo de sincronización con política KEEP.")
    }

    override fun getPendingProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        dao.getPendingProducts()
            .map { list ->
                Log.d("PendingProducts", "Encontrados ${list.size} productos pendientes")
                list.map { it.toDomain() }
            }
            .collect { emit(Resource.Success(it)) }
    }

    override suspend fun updateProductStatus(productId: String, newStatus: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            // Actualiza el campo "status" en el documento de Firestore
            firestore.collection("products")
                .document(productId)
                .update("status", newStatus)
                .await()

            // También actualiza localmente si quieres (opcional)
            dao.updateStatus(productId, newStatus)

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Error al actualizar estado: ${e.message}")
        }
    }

    override fun getPendingProductsFromFirebase(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading)
        try {
            val snapshot = firestore.collection("products")
                .whereEqualTo("status", "pending")
                .get()
                .await()

            val products = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                Product(
                    id = doc.id,
                    sellerId = data["sellerId"] as? String ?: "",
                    brand = data["brand"] as? String ?: "",
                    model = data["model"] as? String ?: "",
                    storage = data["storage"] as? String ?: "",
                    price = (data["price"] as? Number)?.toDouble() ?: 0.0,
                    imei = data["imei"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    images = data["images"] as? List<String> ?: emptyList(),
                    box = data["box"] as? String ?: "",
                    invoiceUri = data["invoiceUri"] as? String ?: "",
                    status = data["status"] as? String ?: "",
                    active = data["active"] as? Boolean ?: true,
                    // Aquí ignoramos Timestamp y ponemos string vacío o una fecha fija si quieres
                    createdAt = "",
                    updatedAt = "",
                    isSynced = data["isSynced"] as? Boolean ?: true
                )
            }

            emit(Resource.Success(products))
        } catch (e: Exception) {
            emit(Resource.Error("Error al obtener productos pendientes: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getProductByIdFromFirebase(productId: String): Resource<Product> = try {
        val doc = firestore.collection("products").document(productId).get().await()
        if (!doc.exists()) {
            Resource.Error("Producto no encontrado en Firestore")
        } else {
            val data = doc.data!!
            Resource.Success(
                Product(
                    id = doc.id,
                    sellerId    = data["sellerId"]  as? String ?: "",
                    brand       = data["brand"]     as? String ?: "",
                    model       = data["model"]     as? String ?: "",
                    storage     = data["storage"]   as? String ?: "",
                    price       = (data["price"] as? Number)?.toDouble() ?: 0.0,
                    imei        = data["imei"]      as? String ?: "",
                    description = data["description"] as? String ?: "",
                    images      = data["images"]    as? List<String> ?: emptyList(),
                    box         = data["box"]       as? String ?: "",
                    invoiceUri  = data["invoiceUri"] as? String ?: "",
                    status      = data["status"]    as? String ?: "",
                    active      = data["active"]    as? Boolean ?: true,
                    createdAt   = "",
                    updatedAt   = "",
                    isSynced    = data["isSynced"]  as? Boolean ?: true
                )
            )
        }
    } catch (e: Exception) {
        Resource.Error("Error Firestore: ${e.message}")
    }




}
