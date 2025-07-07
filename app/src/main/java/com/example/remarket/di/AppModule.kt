package com.example.remarket.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.remarket.data.local.AppDatabase
import com.example.remarket.data.local.ProductDao
import com.example.remarket.data.network.ApiService
import com.example.remarket.data.network.AuthInterceptor
import com.example.remarket.data.network.TokenManager
import com.example.remarket.data.repository.*
import com.example.remarket.domain.usecase.GetProductsUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    // Conectividad
    @Provides
    @Singleton
    fun provideConnectivityRepository(
        @ApplicationContext context: Context
    ): IConnectivityRepository = ConnectivityRepository(context)

    // Token provider dinámico
    @Provides
    @Singleton
    fun provideTokenProvider(tokenManager: TokenManager): () -> String = {
        tokenManager.getToken() ?: ""
    }

    // Interceptor de logs
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    // Cliente HTTP con interceptores
    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

    // Retrofit
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("http://161.132.50.99:9364/") // tu baseURL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ApiService
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        Log.d("HiltModule", "provideApiService() called")
        return retrofit.create(ApiService::class.java)
    }

    // Room database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "remarket_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(db: AppDatabase): ProductDao = db.productDao()

    // CloudinaryService
    @Provides
    @Singleton
    fun provideCloudinaryService(@ApplicationContext context: Context): CloudinaryService {
        return CloudinaryService(context)
    }

    // Firebase Auth
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    // Firebase Firestore
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // ProductRepository completo
    @Provides
    @Singleton
    fun provideProductRepository(
        apiService: ApiService,
        productDao: ProductDao,
        cloudinaryService: CloudinaryService,
        connectivityRepository: IConnectivityRepository,
        firebaseAuth: FirebaseAuth,
        @ApplicationContext context: Context,
        firestore: FirebaseFirestore
    ): IProductRepository = ProductRepository(
        apiService,
        productDao,
        cloudinaryService,
        connectivityRepository,
        firebaseAuth,
        context,
        firestore
    )

    // UserRepository
    @Provides
    @Singleton
    fun provideUserRepository(api: ApiService): UserRepository =
        UserRepository(api)

    // Caso de uso GetProducts
    @Provides
    @Singleton
    fun provideGetProductsUseCase(repo: IProductRepository): GetProductsUseCase =
        GetProductsUseCase(repo)
}
