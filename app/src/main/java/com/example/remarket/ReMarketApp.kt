// ReMarketApp.kt
package com.example.remarket

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ReMarketApp : Application() {

    // 1. Inyectamos la HiltWorkerFactory. Hilt sabe cómo hacer esto.
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // 2. Creamos la configuración para WorkManager, pasándole la factory de Hilt.
        val config = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

        // 3. Inicializamos WorkManager manualmente con nuestra configuración personalizada.
        //    Esto debe hacerse antes de que se use WorkManager en cualquier otro lugar.
        WorkManager.initialize(this, config)
    }
}