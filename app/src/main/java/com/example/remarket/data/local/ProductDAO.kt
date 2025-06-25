// data/local/ProductDao.kt
package com.example.remarket.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
     * Borra todos los productos de la tabla.
     */
    @Query("DELETE FROM products")
    suspend fun deleteAll()
}