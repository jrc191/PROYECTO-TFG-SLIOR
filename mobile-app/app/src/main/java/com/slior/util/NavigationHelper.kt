package com.slior.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.slior.data.local.entity.StopEntity

/**
 * Utilidad para gestionar la navegación externa hacia aplicaciones de mapas (Google Maps, Waze).
 */
object NavigationHelper {

    /**
     * Abre Google Maps con una ruta completa que incluye todas las paradas pendientes.
     * Utiliza el formato de URLs de Google Maps para incluir waypoints (puntos intermedios).
     */
    fun launchExternalNavigation(context: Context, stops: List<StopEntity>) {
        if (stops.isEmpty()) {
            Toast.makeText(context, "No hay paradas en la ruta", Toast.LENGTH_SHORT).show()
            return
        }

        // 1. Filtrar solo paradas pendientes (Slior usa "ENTREGADO" para paradas completadas)
        val pendingStops = stops.filter { it.status != "ENTREGADO" }
        
        if (pendingStops.isEmpty()) {
            Toast.makeText(context, "Todas las paradas ya han sido entregadas", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Definir destino (la última parada) y waypoints (las intermedias)
        val lastStop = pendingStops.last()
        val waypoints = if (pendingStops.size > 1) {
            pendingStops.dropLast(1).joinToString("|") { "${it.latitud},${it.longitud}" }
        } else null

        // 3. Construir la URI para Google Maps usando COORDENADAS EXACTAS
        // Esto evita errores por nombres de calles distintos entre proveedores.
        val uriBuilder = Uri.parse("https://www.google.com/maps/dir/?api=1")
            .buildUpon()
            .appendQueryParameter("destination", "${lastStop.latitud},${lastStop.longitud}")
            .appendQueryParameter("travelmode", "driving")

        if (waypoints != null) {
            uriBuilder.appendQueryParameter("waypoints", waypoints)
        }

        launchIntent(context, uriBuilder.build())
    }

    /**
     * Abre Google Maps para navegar a una única parada específica.
     */
    fun launchSingleStopNavigation(context: Context, stop: StopEntity) {
        val uri = Uri.parse("google.navigation:q=${stop.latitud},${stop.longitud}")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback a URL de Google Maps si la app no responde al esquema navigation:
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${stop.latitud},${stop.longitud}&travelmode=driving")
            launchIntent(context, webUri)
        }
    }

    private fun launchIntent(context: Context, uri: Uri) {
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            val genericIntent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(genericIntent)
        }
    }
}
