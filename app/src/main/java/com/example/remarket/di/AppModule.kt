package com.example.remarket.di

import android.util.Log
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.network.AuthInterceptor
import com.example.remarket.data.repository.ProductRepository
import com.example.remarket.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideAuthInterceptor(): AuthInterceptor {
        Log.d("HiltModule", "provideAuthInterceptor() called")
        return AuthInterceptor { "JrC82jDFni1k00WatL2Z:seller" }

    }
    @Provides @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        Log.d("HiltModule", "provideOkHttpClient() called")
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)    // ① primero el auth
            .addInterceptor(logging)            // ② luego el logging
            .build()
    }
    // Ahora recibe el OkHttpClient para que use authInterceptor
    @Provides @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        Log.d("HiltModule", "provideRetrofit() called with client: $okHttpClient")
        return Retrofit.Builder()
            .baseUrl("http://161.132.50.99:9364/")
            .client(okHttpClient)                // <— aquí
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        Log.d("HiltModule", "provideApiService() called with retrofit: $retrofit")
        return retrofit.create(ApiService::class.java)
    }

    @Provides @Singleton
    fun provideUserRepository(): UserRepository =
        UserRepository()

    // Ahora Hilt inyectará ApiService en el constructor de ProductRepository
    @Provides @Singleton
    fun provideProductRepository(apiService: ApiService): ProductRepository =
        ProductRepository(apiService)
}