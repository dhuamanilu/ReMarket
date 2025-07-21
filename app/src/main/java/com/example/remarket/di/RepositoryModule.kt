package com.example.remarket.di

import com.example.remarket.data.repository.IReportRepository
import com.example.remarket.data.repository.ReportRepository
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
