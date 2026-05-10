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
    val youLabel = stringResource(R.string.map_label_you)
    val selectedLabel = stringResource(R.string.map_label_selected)
    val stopLabelTemplate = stringResource(R.string.map_label_stop)
    val nameLabel = stringResource(R.string.map_info_name)
    val addressLabel = stringResource(R.string.map_info_address)
    val phoneLabel = stringResource(R.string.map_info_phone)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(initialZoom)
                initialCenter?.let { controller.setCenter(GeoPoint(it.first, it.second)) }
                val eventsReceiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean { onLocationSelected(p.latitude, p.longitude); return true }
                    override fun longPressHelper(p: GeoPoint): Boolean { onLocationSelected(p.latitude, p.longitude); return true }
                }
                overlays.add(MapEventsOverlay(eventsReceiver))
                addMapListener(object : org.osmdroid.events.MapListener {
                    override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean { val center = mapCenter; onCenterChanged(center.latitude, center.longitude); return true }
                    override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean { event?.zoomLevel?.let { onZoomChanged(it) }; return true }
                })
            }
        },
        update = { mapView ->
            val customInfoWindow = object : MarkerInfoWindow(org.osmdroid.library.R.layout.bonuspack_bubble, mapView) {
                override fun onOpen(item: Any?) {
                    super.onOpen(item)
                    mapView.overlays.filterIsInstance<Marker>().forEach { if (it != item && it.isInfoWindowShown) it.closeInfoWindow() }
                    val description = view.findViewById<TextView>(org.osmdroid.library.R.id.bubble_description)
                    val spannable = SpannableStringBuilder(Html.fromHtml((item as? Marker)?.snippet ?: "", Html.FROM_HTML_MODE_LEGACY))
                    val spans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
                    for (span in spans) {
                        if (span.url.startsWith("tel:")) {
                            val start = spannable.getSpanStart(span)
                            val end = spannable.getSpanEnd(span)
                            val phone = span.url.substring(4)
                            val clickable = object : ClickableSpan() { override fun onClick(widget: View) { onCallRequest(phone) } }
                            spannable.removeSpan(span); spannable.setSpan(clickable, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        }
                    }
                    description?.movementMethod = LinkMovementMethod.getInstance(); description?.text = spannable
                }
            }

            // 1. Centrado
            if (centerKey != null) {
                if (selectedLocation != null) mapView.controller.animateTo(GeoPoint(selectedLocation.latitude, selectedLocation.longitude))
                else if (defaultCenter != null) mapView.controller.animateTo(GeoPoint(defaultCenter.first, defaultCenter.second))
            } else if (selectedLocation != null) {
                val point = GeoPoint(selectedLocation.latitude, selectedLocation.longitude)
                if (mapView.mapCenter.latitude != point.latitude || mapView.mapCenter.longitude != point.longitude) mapView.controller.animateTo(point)
            } else if (!hasCenteredOnce && defaultCenter != null) {
                mapView.controller.setCenter(GeoPoint(defaultCenter.first, defaultCenter.second)); onCentered()
            }

            // 2. Overlays
            val eventOverlay = mapView.overlays.find { it is MapEventsOverlay }
            mapView.overlays.clear(); eventOverlay?.let { mapView.overlays.add(it) }

            // 3. Paradas existentes
            existingStops.forEachIndexed { index, stop ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(stop.latitud, stop.longitud)
                    title = String.format(stopLabelTemplate, index + 1)
                    infoWindow = customInfoWindow
                    snippet = "<b>${nameLabel.split(":")[0]}:</b> ${stop.destinatario}<br><b>${addressLabel.split(":")[0]}:</b> ${stop.direccion}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    alpha = 0.6f; setOnMarkerClickListener { m, _ -> m.showInfoWindow(); true }
                }
                mapView.overlays.add(marker)
            }

            // 4. Usuario
            defaultCenter?.let { loc ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(loc.first, loc.second); title = youLabel; infoWindow = customInfoWindow; setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    val raw = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    val wrapped = DrawableCompat.wrap(raw); DrawableCompat.setTint(wrapped, Color.parseColor("#FF5722")); DrawableCompat.setTintMode(wrapped, PorterDuff.Mode.SRC_IN)
                    icon = wrapped; showInfoWindow(); setOnMarkerClickListener { m, _ -> m.showInfoWindow(); true }
                }
                mapView.overlays.add(marker)
            }

            // 5. Selección
            selectedLocation?.let { loc ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(loc.latitude, loc.longitude); title = selectedLabel; infoWindow = customInfoWindow
                    snippet = "<b>${addressLabel.split(":")[0]}:</b> ${loc.displayName}"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = mapView.context.resources.getDrawable(org.osmdroid.library.R.drawable.marker_default, null).mutate()
                    showInfoWindow(); setOnMarkerClickListener { m, _ -> m.showInfoWindow(); true }
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        }
    )
}
