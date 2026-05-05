package com.slior.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.slior.data.local.entity.StopEntity
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun RouteMapView(
    stops: List<StopEntity>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(13.0)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            if (stops.isEmpty()) return@AndroidView

            // Centrar el mapa en la primera parada
            val primera = GeoPoint(stops[0].latitud, stops[0].longitud)
            mapView.controller.setCenter(primera)

            // Añadir marcador por cada parada
            stops.forEach { stop ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(stop.latitud, stop.longitud)
                    title = "${stop.ordenVisita}. ${stop.destinatario}"
                    snippet = stop.direccion
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }

            // Dibujar línea conectando las paradas en orden
            if (stops.size >= 2) {
                val polyline = Polyline().apply {
                    setPoints(stops.map { GeoPoint(it.latitud, it.longitud) })
                }
                mapView.overlays.add(polyline)
            }

            mapView.invalidate()
        }
    )
}