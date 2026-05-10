package com.slior.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.slior.data.remote.dto.AddressSuggestion
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import android.graphics.Color
import android.graphics.PorterDuff
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
import androidx.compose.ui.res.stringResource
import com.slior.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Mapa interactivo para seleccionar una ubicación.
 */
@Composable
fun PlacePickerMap(
    selectedLocation: AddressSuggestion?,
    onLocationSelected: (Double, Double) -> Unit,
    modifier: Modifier = Modifier,
    defaultCenter: Pair<Double, Double>? = null,
    hasCenteredOnce: Boolean = false,
    onCentered: () -> Unit = {},
    initialZoom: Double = 15.0,
    onZoomChanged: (Double) -> Unit = {},
    initialCenter: Pair<Double, Double>? = null,
    onCenterChanged: (Double, Double) -> Unit = { _, _ -> },
    existingStops: List<RouteMapPoint> = emptyList(),
    centerKey: Any? = null,
    onCallRequest: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(initialZoom)
            initialCenter?.let { controller.setCenter(GeoPoint(it.first, it.second)) }
            
            val eventsReceiver = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { 
                    onLocationSelected(p.latitude, p.longitude)
                    return true 
                }
                override fun longPressHelper(p: GeoPoint): Boolean { 
                    onLocationSelected(p.latitude, p.longitude)
                    return true 
                }
            }
            overlays.add(MapEventsOverlay(eventsReceiver))
            
            addMapListener(object : org.osmdroid.events.MapListener {
                override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                    val center = mapCenter
                    onCenterChanged(center.latitude, center.longitude)
                    return true
                }
                override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                    event?.zoomLevel?.let { onZoomChanged(it) }
                    return true
                }
            })
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
    val selectedLabel = stringResource(R.string.map_label_selected)
    val stopLabelTemplate = stringResource(R.string.map_label_stop)
    val nameLabel = stringResource(R.string.map_info_name)
    val addressLabel = stringResource(R.string.map_info_address)
    val phoneLabel = stringResource(R.string.map_info_phone)

    var lastCenterKey by remember { mutableStateOf<Any?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { m ->
            // Reasegurar el User Agent en cada actualización para evitar bloqueos
            Configuration.getInstance().userAgentValue = "SliorLogistics_TFG_App_${context.packageName}"

            // Limpiar overlays previos (excepto el de eventos)
            val eventOverlay = m.overlays.find { it is MapEventsOverlay }
            
            // Cerrar burbujas informativas previas para evitar "ghosting"
            m.overlays.filterIsInstance<Marker>().forEach { it.closeInfoWindow() }
            
            m.overlays.clear()
            eventOverlay?.let { m.overlays.add(it) }

            val customInfoWindow = object : MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, m) {
                override fun onOpen(item: Any?) {
                    super.onOpen(item)
                    // Cerrar otras burbujas al abrir una nueva
                    m.overlays.filterIsInstance<Marker>().forEach { 
                        if (it != item && it.isInfoWindowShown) it.closeInfoWindow() 
                    }
                    val marker = item as? Marker
                    val description = view.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)
                    val snip = marker?.snippet ?: ""
                    val spannable = SpannableStringBuilder(Html.fromHtml(snip, Html.FROM_HTML_MODE_LEGACY))
                    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                    for (span in spans) {
                        if (span.url.startsWith("tel:")) {
                            val start = spannable.getSpanStart(span)
                            val end = spannable.getSpanEnd(span)
                            val phone = span.url.substring(4)
                            val clickable = object : ClickableSpan() { 
                                override fun onClick(widget: View) { onCallRequest(phone) } 
                            }
                            spannable.removeSpan(span)
                            spannable.setSpan(clickable, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    description?.movementMethod = LinkMovementMethod.getInstance()
                    description?.text = spannable
                }
            }

            // 1. Centrado y Zoom Automático
            if (centerKey != null && centerKey != lastCenterKey) {
                val allPoints = mutableListOf<GeoPoint>()
                defaultCenter?.let { if (it.first != 0.0 || it.second != 0.0) allPoints.add(GeoPoint(it.first, it.second)) }
                existingStops.forEach { if (it.latitud != 0.0 || it.longitud != 0.0) allPoints.add(GeoPoint(it.latitud, it.longitud)) }
                selectedLocation?.let { if (it.latitude != 0.0 || it.longitude != 0.0) allPoints.add(GeoPoint(it.latitude, it.longitude)) }
                
                if (allPoints.isNotEmpty()) {
                    if (allPoints.size == 1) {
                        m.controller.setZoom(15.0)
                        m.controller.animateTo(allPoints[0])
                    } else {
                        val box = org.osmdroid.util.BoundingBox.fromGeoPoints(allPoints)
                        m.post { m.zoomToBoundingBox(box, true, 120, 18.0, 500) }
                    }
                    lastCenterKey = centerKey
                }
            } else if (selectedLocation != null) {
                val point = GeoPoint(selectedLocation.latitude, selectedLocation.longitude)
                // Solo animar si la posición seleccionada cambió significativamente (para evitar bucles de animación)
                if (Math.abs(m.mapCenter.latitude - point.latitude) > 0.000001 || 
                    Math.abs(m.mapCenter.longitude - point.longitude) > 0.000001) {
                    m.controller.animateTo(point)
                }
            } else if (!hasCenteredOnce && defaultCenter != null) {
                m.controller.setCenter(GeoPoint(defaultCenter.first, defaultCenter.second))
                onCentered()
            }

            // 2. Paradas existentes (Markers estables)
            existingStops.forEachIndexed { index, stop ->
                val marker = Marker(m).apply {
                    position = GeoPoint(stop.latitud, stop.longitud)
                    title = String.format(stopLabelTemplate, index + 1)
                    infoWindow = customInfoWindow
                    snippet = "<b>${nameLabel.split(":")[0]}:</b> ${stop.destinatario}<br><b>${addressLabel.split(":")[0]}:</b> ${stop.direccion}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    
                    val raw = context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrapped = DrawableCompat.wrap(raw)
                    if (stop.isCompleted) {
                        DrawableCompat.setTint(wrapped, Color.parseColor("#4CAF50"))
                        alpha = 0.5f
                    } else {
                        DrawableCompat.setTint(wrapped, Color.parseColor("#2196F3"))
                        alpha = 0.7f
                    }
                    icon = wrapped
                    setOnMarkerClickListener { mark, _ -> mark.showInfoWindow(); true }
                }
                m.overlays.add(marker)
            }

            // 3. Usuario (Marker Courier)
            defaultCenter?.let { loc ->
                val marker = Marker(m).apply {
                    position = GeoPoint(loc.first, loc.second)
                    title = youLabel
                    infoWindow = customInfoWindow
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val raw = context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrapped = DrawableCompat.wrap(raw)
                    DrawableCompat.setTint(wrapped, Color.parseColor("#FF5722"))
                    DrawableCompat.setTintMode(wrapped, PorterDuff.Mode.SRC_IN)
                    icon = wrapped
                    setOnMarkerClickListener { mark, _ -> mark.showInfoWindow(); true }
                }
                m.overlays.add(marker)
            }

            // 4. SELECCIÓN ACTUAL (Solo debe haber una)
            selectedLocation?.let { loc ->
                val marker = Marker(m).apply {
                    position = GeoPoint(loc.latitude, loc.longitude)
                    title = selectedLabel
                    infoWindow = customInfoWindow
                    snippet = "<b>${addressLabel.split(":")[0]}:</b> ${loc.displayName}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    // Usar un color distinto (Verde Neón) para la selección actual
                    val raw = context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrapped = DrawableCompat.wrap(raw)
                    DrawableCompat.setTint(wrapped, Color.parseColor("#39FF14"))
                    icon = wrapped
                    
                    setOnMarkerClickListener { mark, _ -> mark.showInfoWindow(); true }
                }
                m.overlays.add(marker)
                // Abrir automáticamente la burbuja de la selección para confirmar que es la correcta
                marker.showInfoWindow()
            }
            
            m.invalidate()
        }
    )
}
