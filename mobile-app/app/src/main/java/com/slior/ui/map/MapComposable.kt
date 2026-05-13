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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.slior.R

@Composable
fun RouteMapView(
    stops: List<RouteMapPoint>,
    modifier: Modifier = Modifier,
    userLocation: Pair<Double, Double>? = null,
    centerKey: Any? = null,
    onCallRequest: (String) -> Unit = {}
) {
    val context = LocalContext.current
    
    // Estado para controlar el centrado inicial y manual
    var hasCenteredOnce by remember { mutableStateOf(false) }
    var lastCenterKey by remember { mutableStateOf<Any?>(null) }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(13.0)
            setDestroyMode(false)
        }
    }

    // Gestión del ciclo de vida de osmdroid
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_RESUME -> mapView.onResume()
                androidx.lifecycle.Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    val youLabel = stringResource(R.string.map_label_you)
    val stopLabelTemplate = stringResource(R.string.map_label_stop)
    val nameLabel = stringResource(R.string.map_info_name)
    val addressLabel = stringResource(R.string.map_info_address)
    val phoneLabel = stringResource(R.string.map_info_phone)

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { m ->
            Configuration.getInstance().userAgentValue = "SliorLogistics_TFG_App_${context.packageName}"
            
            m.overlays.clear()

            val customInfoWindow = object : MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, m) {
                override fun onOpen(item: Any?) {
                    super.onOpen(item)
                    m.overlays.filterIsInstance<Marker>().forEach { 
                        if (it != item && it.isInfoWindowShown) it.closeInfoWindow()
                    }
                    val marker = item as? Marker
                    val description = view.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)
                    val htmlContent = Html.fromHtml(marker?.snippet ?: "", Html.FROM_HTML_MODE_LEGACY)
                    val spannable = SpannableStringBuilder(htmlContent)
                    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                    for (span in spans) {
                        if (span.url.startsWith("tel:")) {
                            val start = spannable.getSpanStart(span)
                            val end = spannable.getSpanEnd(span)
                            val phoneNumber = span.url.substring(4)
                            val clickableSpan = object : ClickableSpan() {
                                override fun onClick(widget: View) { onCallRequest(phoneNumber) }
                            }
                            spannable.removeSpan(span)
                            spannable.setSpan(clickableSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    description?.movementMethod = LinkMovementMethod.getInstance()
                    description?.text = spannable
                }
            }

            // 1. Recopilar puntos para trazado y centrado
            val geoPoints = mutableListOf<GeoPoint>()
            userLocation?.let { if (it.first != 0.0) geoPoints.add(GeoPoint(it.first, it.second)) }
            geoPoints.addAll(stops.map { GeoPoint(it.latitud, it.longitud) })

            // 2. Línea de ruta (Polyline)
            if (geoPoints.size >= 2) {
                val polyline = Polyline(m).apply {
                    setPoints(geoPoints)
                    outlinePaint.color = Color.BLACK
                    outlinePaint.strokeWidth = 10f
                    outlinePaint.isAntiAlias = true
                }
                m.overlays.add(polyline)
            }

            // 3. Marcador de Usuario
            userLocation?.let {
                if (it.first != 0.0) {
                    val courierMarker = Marker(m).apply {
                        position = GeoPoint(it.first, it.second)
                        title = youLabel
                        infoWindow = customInfoWindow
                        val rawDrawable = context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                        val wrappedDrawable = DrawableCompat.wrap(rawDrawable)
                        DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#FF5722"))
                        DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
                        icon = wrappedDrawable
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        setOnMarkerClickListener { mark, _ -> mark.showInfoWindow(); true }
                    }
                    m.overlays.add(courierMarker)
                }
            }

            // 4. Marcadores de Paradas
            stops.forEachIndexed { index, stop ->
                val marker = Marker(m).apply {
                    position = GeoPoint(stop.latitud, stop.longitud)
                    title = String.format(stopLabelTemplate, index + 1)
                    infoWindow = customInfoWindow
                    
                    val sb = StringBuilder()
                    sb.append("<b>${nameLabel.split(":")[0]}:</b> ${stop.destinatario}<br>")
                    sb.append("<b>${addressLabel.split(":")[0]}:</b> ${stop.direccion}<br>")
                    if (stop.telefono.isNotEmpty()) {
                        sb.append("<b>${phoneLabel.split(":")[0]}:</b> <a href=\"tel:${stop.telefono}\">${stop.telefono}</a>")
                    }
                    snippet = sb.toString()
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    val rawDrawable = context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrappedDrawable = DrawableCompat.wrap(rawDrawable)
                    if (stop.isCompleted) {
                        DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#4CAF50"))
                        alpha = 0.7f
                    } else {
                        DrawableCompat.setTint(wrappedDrawable, Color.parseColor("#2196F3"))
                    }
                    icon = wrappedDrawable
                    setOnMarkerClickListener { mark, _ -> mark.showInfoWindow(); true }
                }
                m.overlays.add(marker)
            }

            // 5. Centrado Inteligente
            val isManualCenter = centerKey != null && centerKey != lastCenterKey
            val validPoints = geoPoints.filter { it.latitude != 0.0 && it.longitude != 0.0 }
            
            if ((!hasCenteredOnce || isManualCenter) && validPoints.isNotEmpty()) {
                if (validPoints.size == 1) {
                    m.controller.setZoom(15.0)
                    m.controller.animateTo(validPoints[0])
                } else {
                    val boundingBox = org.osmdroid.util.BoundingBox.fromGeoPoints(validPoints)
                    m.post { 
                        m.zoomToBoundingBox(boundingBox, true, 120, 17.0, 500)
                    }
                }
                hasCenteredOnce = true
                lastCenterKey = centerKey
            }
            
            m.invalidate()
        }
    )
}

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
