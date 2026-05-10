package com.slior

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import org.osmdroid.config.Configuration as OsmConfig

@HiltAndroidApp
class SliorApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        
        // Inicialización de osmdroid (Forma estándar y segura)
        val osmConfig = OsmConfig.getInstance()
        
        // 1. Establecer el User Agent (MUY específico para evitar bloqueos del servidor OSM)
        osmConfig.userAgentValue = "SliorLogistics_TFG_App_${packageName}"
        
        // 2. Forzar el uso de la caché interna de la app para evitar problemas de permisos en Android 10+
        val mapCache = java.io.File(cacheDir, "osmdroid_tiles")
        if (!mapCache.exists()) mapCache.mkdirs()
        osmConfig.osmdroidTileCache = mapCache
        
        // 3. Cargar configuración previa
        osmConfig.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
