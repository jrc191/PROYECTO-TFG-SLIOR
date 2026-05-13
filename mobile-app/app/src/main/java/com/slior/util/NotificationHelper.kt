package com.slior.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.slior.MainActivity
import com.slior.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utilidad centralizada para gestionar las notificaciones de la aplicación.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ROUTES_ID = "slior_routes_channel"
        const val CHANNEL_SYNC_ID = "slior_sync_channel"
        const val CHANNEL_SYSTEM_ID = "slior_system_channel"
        
        const val NOTIF_SYNC_ID = 1001
        const val NOTIF_ROUTE_REMINDER_ID = 1002
        const val NOTIF_CONNECTIVITY_ID = 1003
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal para Rutas y Entregas
            val routeChannel = NotificationChannel(
                CHANNEL_ROUTES_ID,
                "Rutas y Entregas",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre el estado de tus rutas y confirmaciones de entrega."
            }

            // Canal para Sincronización (Prioridad baja para no molestar)
            val syncChannel = NotificationChannel(
                CHANNEL_SYNC_ID,
                "Sincronización de Datos",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Estado de la sincronización con el servidor central."
            }

            // Canal para Estado del Sistema
            val systemChannel = NotificationChannel(
                CHANNEL_SYSTEM_ID,
                "Estado del Sistema",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas críticas sobre la conexión o el estado de la cuenta."
            }

            manager.createNotificationChannel(routeChannel)
            manager.createNotificationChannel(syncChannel)
            manager.createNotificationChannel(systemChannel)
        }
    }

    /**
     * Muestra una notificación de éxito tras sincronizar paradas pendientes.
     */
    fun showSyncSuccessNotification(count: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_SYNC_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Usar icono de la app
            .setContentTitle("Sincronización Completada")
            .setContentText("Se han subido $count paradas pendientes con éxito.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setColor(0xFF39FF14.toInt()) // Verde Neón SLIOR

        notifySafe(NOTIF_SYNC_ID, builder.build())
    }

    /**
     * Notifica un cambio crítico en la conectividad del servidor.
     */
    fun showConnectivityAlert(isOnline: Boolean) {
        val title = if (isOnline) "Servidor Restablecido" else "Modo Offline Activo"
        val text = if (isOnline) "Vuelves a estar conectado. Tus datos se sincronizarán pronto." 
                   else "Se ha perdido la conexión. Puedes seguir trabajando en modo local."
        val icon = if (isOnline) R.drawable.ic_launcher_foreground else android.R.drawable.ic_dialog_alert

        val builder = NotificationCompat.Builder(context, CHANNEL_SYSTEM_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setColor(if (isOnline) 0xFF39FF14.toInt() else 0xFFF95B06.toInt()) // Neón o Naranja

        notifySafe(NOTIF_CONNECTIVITY_ID, builder.build())
    }

    /**
     * Envía una notificación genérica de recordatorio de ruta.
     */
    fun showRouteReminder(routeName: String, stopCount: Int) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ROUTES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Ruta Pendiente: $routeName")
            .setContentText("Tienes $stopCount paradas esperando para ser entregadas.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(0xFF39FF14.toInt())

        notifySafe(NOTIF_ROUTE_REMINDER_ID, builder.build())
    }

    /**
     * Notifica cuando el repartidor está cerca de una parada.
     */
    fun showProximityAlert(stopName: String, distance: Double) {
        val distText = if (distance < 1000) "${distance.toInt()} m" else "%.1f km".format(distance / 1000)
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ROUTES_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Próxima Parada: $stopName")
            .setContentText("Estás a $distText de tu destino.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setColor(0xFF39FF14.toInt())

        notifySafe(1004, builder.build())
    }

    private fun notifySafe(id: Int, notification: android.app.Notification) {
        try {
            val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context, 
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true // En versiones anteriores a Android 13 no hace falta este permiso
            }

            if (hasPermission) {
                NotificationManagerCompat.from(context).notify(id, notification)
            }
        } catch (e: SecurityException) {
            // Permiso denegado en tiempo de ejecución
        }
    }
}
