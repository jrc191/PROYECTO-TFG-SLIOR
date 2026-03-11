package com.slior.di

import android.content.Context
import androidx.room.Room
import com.slior.data.local.AppDatabase
import com.slior.data.local.dao.RouteDao
import com.slior.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Módulo Hilt que provee las dependencias de base de datos.
 *
 * @InstallIn(SingletonComponent) → las instancias viven mientras
 * vive la aplicación (singleton global).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Crea la instancia única de Room Database.
     * fallbackToDestructiveMigration() → si la versión de la BD
     * no coincide, la recrea (solo aceptable en desarrollo).
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    /** Provee el UserDAO a partir de la base de datos. */
    @Provides
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRouteDao(database: AppDatabase): RouteDao {
        return database.routeDao()
    }
}