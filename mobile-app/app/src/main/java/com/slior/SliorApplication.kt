package com.slior

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Clase Application de SLIOR.
 *
 * Anotada con @HiltAndroidApp para que Hilt genere los componentes
 * de inyección de dependencias al arrancar la app.
 *
 * También implementa [Configuration.Provider] para que WorkManager
 * use HiltWorkerFactory y pueda inyectar dependencias en los Workers.
 */
@HiltAndroidApp
class SliorApplication : Application(), Configuration.Provider {

    /**
     * Inyectado por Hilt. Necesario para que WorkManager use Workers con @HiltWorker.
     */
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * Proporciona la configuración personalizada de WorkManager con Hilt.
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
