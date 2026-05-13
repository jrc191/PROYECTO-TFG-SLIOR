package com.slior.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.slior.data.local.entity.StopEntity

/**
 * Utilidad para gestionar la navegación externa hacia aplicaciones de mapas.
 * Orden de prioridad: Google Maps → Waze → navegador/chooser genérico.
 *
 * Nota: Waze no soporta waypoints intermedios vía deep link, por lo que
 * en rutas multi-parada navega únicamente al destino final.
 */
object NavigationHelper {

    /**
     * Abre Google Maps (o Waze como fallback) con una ruta completa
     * que incluye todas las paradas pendientes como waypoints.
     */
    fun launchExternalNavigation(context: Context, stops: List<StopEntity>) {
        if (stops.isEmpty()) {
            Toast.makeText(context, "No hay paradas en la ruta", Toast.LENGTH_SHORT).show()
            return
        }

        val pendingStops = stops.filter { it.status != "ENTREGADO" }

        if (pendingStops.isEmpty()) {
            Toast.makeText(context, "Todas las paradas ya han sido entregadas", Toast.LENGTH_SHORT).show()
            return
        }

        val lastStop = pendingStops.last()
        val waypoints = if (pendingStops.size > 1) {
            pendingStops.dropLast(1).joinToString("|") { "${it.latitud},${it.longitud}" }
        } else null

        val uriBuilder = Uri.parse("https://www.google.com/maps/dir/?api=1")
            .buildUpon()
            .appendQueryParameter("destination", "${lastStop.latitud},${lastStop.longitud}")
            .appendQueryParameter("travelmode", "driving")

        if (waypoints != null) {
            uriBuilder.appendQueryParameter("waypoints", waypoints)
        }

        launchIntent(
            context    = context,
            mapsUri    = uriBuilder.build(),
            destLat    = lastStop.latitud,
            destLon    = lastStop.longitud
        )
    }

    /**
     * Abre Google Maps (o Waze como fallback) para navegar a una única parada.
     */
    fun launchSingleStopNavigation(context: Context, stop: StopEntity) {
        // Intentar primero con el esquema nativo de Google Maps (más directo)
        val nativeUri = Uri.parse("google.navigation:q=${stop.latitud},${stop.longitud}")
        val nativeIntent = Intent(Intent.ACTION_VIEW, nativeUri)
            .apply { setPackage("com.google.android.apps.maps") }

        if (nativeIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(nativeIntent)
            return
        }

        // Si el esquema nativo falla, usar launchIntent con URL web + fallback a Waze
        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1")
            .buildUpon()
            .appendQueryParameter("destination", "${stop.latitud},${stop.longitud}")
            .appendQueryParameter("travelmode", "driving")
            .build()

        launchIntent(
            context = context,
            mapsUri = webUri,
            destLat = stop.latitud,
            destLon = stop.longitud
        )
    }

    /**
     * Lanza la intent de navegación en orden: Google Maps → Waze → genérico.
     *
     * @param mapsUri  URI con formato Google Maps (web o deep link)
     * @param destLat  Latitud del destino final (usado por Waze)
     * @param destLon  Longitud del destino final (usado por Waze)
     */
    private fun launchIntent(
        context: Context,
        mapsUri: Uri,
        destLat: Double,
        destLon: Double
    ) {
        // 1. Google Maps
        val mapsIntent = Intent(Intent.ACTION_VIEW, mapsUri)
            .apply { setPackage("com.google.android.apps.maps") }
        if (mapsIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapsIntent)
            return
        }

        // 2. Waze — solo soporta destino único vía deep link, sin waypoints
        val wazeUri = Uri.parse("waze://?ll=$destLat,$destLon&navigate=yes")
        val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri)
            .apply { setPackage("com.waze") }
        if (wazeIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(wazeIntent)
            return
        }

        // 3. Fallback genérico: Android presenta el chooser con cualquier app compatible
        context.startActivity(Intent(Intent.ACTION_VIEW, mapsUri))
    }
}