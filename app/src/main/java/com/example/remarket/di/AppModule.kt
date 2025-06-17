package com.example.remarket.di

import android.util.Log
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.network.AuthInterceptor
import com.example.remarket.data.repository.IProductRepository
import com.example.remarket.data.repository.ProductRepository
import com.example.remarket.data.repository.UserRepository
import com.example.remarket.domain.usecase.GetProductsUseCase
import com.google.android.gms.tasks.Tasks
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // 1️⃣ Provider de token "hardcodeado"
    @Provides @Singleton
    fun provideTokenProvider(): () -> String = {
        // Token truncado que ya sabes que funciona
        "JrC82jDFni1k00WatL2Z:seller"
    }
    // 1️⃣ Retrofit

    @Provides @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor // si lo tienes definido e inyectable
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true) // ✅ esto ayuda
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://161.132.50.99:9364/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        Log.d("HiltModule", "provideApiService() called with retrofit: $retrofit")
        return retrofit.create(ApiService::class.java)
    }

    // 2️⃣ Repositorios

    @Provides @Singleton
    fun provideProductRepository(
        apiService: ApiService
    ): IProductRepository = ProductRepository(apiService)
    // Fíjate que devolvemos la INTERFAZ IProductRepository

    @Provides
    @Singleton
    fun provideUserRepository(api: ApiService): UserRepository =
        UserRepository(api)

    @Provides @Singleton
    fun provideGetProductsUseCase(repo: IProductRepository): GetProductsUseCase =
        GetProductsUseCase(repo)
}
