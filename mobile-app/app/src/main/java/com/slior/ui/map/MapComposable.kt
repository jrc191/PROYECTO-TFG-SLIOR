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
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.DrawableCompat
import android.text.Html
import android.widget.TextView
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow
import android.text.method.LinkMovementMethod
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.View

@Composable
fun RouteMapView(
    stops: List<RouteMapPoint>,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    centerKey: Any? = null,
    onCallRequest: (String) -> Unit = {} // Nueva callback para manejar la llamada externamente
) {
    var hasCentered by remember { mutableStateOf(false) }

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

            // InfoWindow personalizado que intercepta el click del teléfono
            val customInfoWindow = object : MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {
                override fun onOpen(item: Any?) {
                    super.onOpen(item)
                    mapView.overlays.filterIsInstance<Marker>().forEach { 
                        if (it != item && it.isInfoWindowShown) it.closeInfoWindow()
                    }
                    val m = item as? Marker
                    val description = view.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)
                    
                    val htmlContent = Html.fromHtml(m?.snippet ?: "", Html.FROM_HTML_MODE_LEGACY)
                    val spannable = SpannableStringBuilder(htmlContent)
                    
                    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                    for (span in spans) {
                        if (span.url.startsWith("tel:")) {
                            val start = spannable.getSpanStart(span)
                            val end = spannable.getSpanEnd(span)
                            val phoneNumber = span.url.substring(4)
                            
                            // Reemplazamos el comportamiento por defecto con nuestra callback
                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) {
                                    onCallRequest(phoneNumber)
                                }
                            }
                            spannable.removeSpan(span)
                            spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    
                    description?.movementMethod = LinkMovementMethod.getInstance()
                    description?.text = spannable
                }
            }

            // 1. Línea de ruta
            val points = mutableListOf<GeoPoint>()
            userLocation?.let { points.add(GeoPoint(it.first, it.second)) }
            points.addAll(stops.map { GeoPoint(it.latitud, it.longitud) })

            if (points.size >= 2) {
                val polyline = Polyline(mapView).apply {
                    setPoints(points)
                    outlinePaint.color = Color.BLACK
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.isAntiAlias = true
                }
                mapView.overlays.add(polyline)
            }

            // 2. Marcador de USUARIO
            userLocation?.let {
                val courierMarker = Marker(mapView).apply {
                    position = GeoPoint(it.first, it.second)
                    title = "USTED ESTÁ AQUÍ"
                    infoWindow = customInfoWindow
                    
                    val rawDrawable = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrappedDrawable = DrawableCompat.wrap(rawDrawable)
                    DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#FF5722"))
                    DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
                    
                    icon = wrappedDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { m, _ -> m.showInfoWindow(); true }
                }
                mapView.overlays.add(courierMarker)
            }

            // 3. Marcadores de PARADAS
            stops.forEachIndexed { index, stop ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(stop.latitud, stop.longitud)
                    title = "PARADA ${index + 1}"
                    infoWindow = customInfoWindow
                    
                    val sb = StringBuilder()
                    sb.append("<b>Nombre:</b> ${stop.destinatario}<br>")
                    sb.append("<b>Dirección:</b> ${stop.direccion}<br>")

                    if (stop.telefono.isNotEmpty()) {
                        sb.append("<b>Teléfono:</b> <a href=\"tel:${stop.telefono}\">${stop.telefono}</a>")
                    }
                    snippet = sb.toString()

                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    
                    if (stop.isCompleted) {
                        alpha = 0.5f
                    }
                    setOnMarkerClickListener { m, _ -> m.showInfoWindow(); true }
                }
                mapView.overlays.add(marker)
            }

            // 4. Centrado
            val shouldCenter = !hasCentered || centerKey != null
            if (shouldCenter) {
                if (stops.isNotEmpty()) {
                    val lastStop = stops.last()
                    mapView.controller.animateTo(GeoPoint(lastStop.latitud, lastStop.longitud))
                } else if (userLocation != null) {
                    mapView.controller.animateTo(GeoPoint(userLocation.first, userLocation.second))
                }
                hasCentered = true
            }

            mapView.invalidate()
        }
    )
}

/** Modelo de datos genérico para mostrar puntos en el mapa */
data class RouteMapPoint(
    val latitud: Double,
    val longitud: Double,
    val direccion: String,
    val destinatario: String,
    val isCompleted: Boolean = false,
    val telefono: String = ""
)

fun StopEntity.toRouteMapPoint() = RouteMapPoint(
    latitud = this.latitud,
    longitud = this.longitud,
    direccion = this.direccion,
    destinatario = this.destinatario,
    isCompleted = this.status.uppercase() == "ENTREGADO",
    telefono = this.telefonoDestinatario
)

fun com.slior.data.remote.dto.StopRequestDto.toRouteMapPoint() = RouteMapPoint(
    latitud = this.latitud,
    longitud = this.longitud,
    direccion = this.direccion,
    destinatario = this.destinatario,
    isCompleted = false,
    telefono = this.telefonoDestinatario
)
