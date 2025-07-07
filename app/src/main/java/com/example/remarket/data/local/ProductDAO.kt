// data/local/ProductDao.kt
package com.example.remarket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.remarket.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    /**
     * Obtiene todos los productos de la base de datos.
     * Devuelve un Flow para que la UI se actualice automáticamente.
     */
    @Query("SELECT * FROM products")
    fun getProducts(): Flow<List<ProductEntity>>

    /**
     * Inserta una lista de productos. Si un producto ya existe, lo reemplaza.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)
    /**
     * Inserta un solo producto. Usado para la creación offline.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity)

    /**
     * Actualiza un producto existente.
     */
    @Update
    suspend fun update(product: ProductEntity)

    /**
     * Obtiene todos los productos que fueron creados offline y no se han subido.
     */
    @Query("SELECT * FROM products WHERE isSynced = 0")
    suspend fun getUnsyncedProducts(): List<ProductEntity>

    /**
     * Borra un producto de la base de datos local por su ID.
     * Usado después de sincronizar la creación con el servidor para reemplazar
     * el producto local (con ID temporal) por el del servidor (con ID final).
     */
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * Borra todos los productos que ya estaban sincronizados.
     * Esto se usa para limpiar la cache local antes de insertar los datos frescos de la red,
     * pero preserva los productos creados offline (isSynced = 0).
     */
    @Query("DELETE FROM products WHERE isSynced = 1") // <-- MÉTODO NUEVO/MODIFICADO
    suspend fun deleteSynced()

    /** ➊ Lista en tiempo real todos los productos con estado PENDING. */
    @Query("SELECT * FROM products WHERE status = 'pending'")
    fun getPendingProducts(): Flow<List<ProductEntity>>

    /** ➋ Actualiza el estado (‘approved’ o ‘rejected’) localmente. */
    @Query("UPDATE products SET status = :newStatus WHERE id = :id")
    suspend fun updateStatus(id: String, newStatus: String)
}