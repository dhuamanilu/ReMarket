// data/local/AppDatabase.kt
package com.example.remarket.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.remarket.data.model.ProductEntity
import com.example.remarket.data.util.Converters

@Database(entities = [ProductEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}