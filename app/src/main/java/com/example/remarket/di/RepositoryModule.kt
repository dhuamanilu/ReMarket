// File: app/src/main/java/com/example/remarket/di/RepositoryModule.kt
package com.example.remarket.di

import com.example.remarket.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepository
    ): IReportRepository
}
