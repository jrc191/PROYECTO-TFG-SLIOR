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

        // 1. Filtrar solo paradas pendientes (opcional, aquí enviamos todas las que no estén entregadas)
        val pendingStops = stops.filter { it.status != "DELIVERED" }
        
        if (pendingStops.isEmpty()) {
            Toast.makeText(context, "Todas las paradas ya han sido entregadas", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Definir destino (la última parada) y waypoints (las intermedias)
        val lastStop = pendingStops.last()
        val waypoints = if (pendingStops.size > 1) {
            pendingStops.dropLast(1).joinToString("|") { "${it.latitud},${it.longitud}" }
        } else null

        // 3. Construir la URI para Google Maps
        // api=1: Versión de la API de Google Maps URLs
        // destination: lat,lng de la última parada
        // waypoints: lista de lat,lng separados por '|'
        // travelmode: driving
        val uriBuilder = Uri.parse("https://www.google.com/maps/dir/?api=1")
            .buildUpon()
            .appendQueryParameter("destination", "${lastStop.latitud},${lastStop.longitud}")
            .appendQueryParameter("travelmode", "driving")

        if (waypoints != null) {
            uriBuilder.appendQueryParameter("waypoints", waypoints)
        }

        val mapUri = uriBuilder.build()

        // 4. Lanzar el Intent
        val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
        mapIntent.setPackage("com.google.android.apps.maps") // Forzar Google Maps si está instalado

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Si Google Maps no está, lanzar un Intent genérico para que el usuario elija (Waze, Maps, Browser)
            val genericIntent = Intent(Intent.ACTION_VIEW, mapUri)
            context.startActivity(genericIntent)
        }
    }
}
