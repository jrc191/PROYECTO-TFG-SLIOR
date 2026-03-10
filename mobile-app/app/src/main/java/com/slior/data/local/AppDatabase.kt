package com.slior.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.slior.data.local.dao.RouteDao
import com.slior.data.local.dao.UserDao
import com.slior.data.local.entity.RouteEntity
import com.slior.data.local.entity.StopEntity
import com.slior.data.local.entity.UserEntity
/**
 * Base de datos Room de SLIOR.
 *
 * Actúa como punto de acceso principal a la base de datos SQLite local.
 * La versión (version = 1) debe incrementarse cada vez que se cambie
 * el esquema (añadir tablas, columnas, etc.) junto con una Migration.
 *
 * Se irán añadiendo más entidades en fases posteriores:
 * RouteEntity, StopEntity, PackageEntity, SyncQueue.
 */
@Database(
    entities = [UserEntity::class, RouteEntity::class, StopEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun routeDao(): RouteDao

    companion object {
        const val DATABASE_NAME = "slior_database"
    }
}