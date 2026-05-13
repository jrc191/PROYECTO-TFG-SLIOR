package com.slior.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.StopRequestDto
import com.slior.data.remote.dto.UpdateRouteRequest
import com.slior.ui.components.PlacePickerScreen
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.map.toRouteMapPoint
import com.slior.ui.map.RouteMapPoint
import com.slior.ui.theme.*
import com.slior.util.Result
import com.slior.util.Validators
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Pantalla de Creación/Edición de Rutas de SLIOR.
 * Totalmente compatible con Modo Claro/Oscuro y Landscape.
 */
@Composable
fun CreateRouteScreen(
    repartidorId: String,
    routeId: String? = null,
    onBack: () -> Unit,
    onRouteCreated: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: com.slior.viewmodel.AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    
    // Colores dinámicos del tema
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    val isRefreshing = detailState is RouteDetailState.Loading && routeId != null
    var centerTrigger by remember { mutableStateOf(0) }
    var phoneToCall by remember { mutableStateOf<String?>(null) }

    var nombre        by rememberSaveable { mutableStateOf("") }
    var fecha         by rememberSaveable { mutableStateOf("") }
    var notas         by rememberSaveable { mutableStateOf("") }
    val paradas       = remember { mutableStateListOf<StopRequestDto>() }

    var stopDireccion    by rememberSaveable { mutableStateOf("") }
    var stopDestinatario by rememberSaveable { mutableStateOf("") }
    var stopTelefono     by rememberSaveable { mutableStateOf("") }
    var stopLat          by rememberSaveable { mutableStateOf("") }
    var stopLon          by rememberSaveable { mutableStateOf("") }
    var stopNotas        by rememberSaveable { mutableStateOf("") }

    var mostrarPlacePicker by rememberSaveable { mutableStateOf(false) }
    var mostrarFormParada by rememberSaveable { mutableStateOf(false) }

    var stopPhoneError   by rememberSaveable { mutableStateOf("") }
    var stopLatError     by rememberSaveable { mutableStateOf("") }
    var stopLonError     by rememberSaveable { mutableStateOf("") }
    var stopDestinatarioError by rememberSaveable { mutableStateOf("") }
    var fechaError       by rememberSaveable { mutableStateOf("") }
    var nombreError      by rememberSaveable { mutableStateOf("") }

    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var stopToDeleteIndex by remember { mutableStateOf<Int?>(null) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.fetchCurrentLocation()
            mostrarPlacePicker = true
        } else {
            mostrarPlacePicker = true
        }
    }

    LaunchedEffect(routeId) { if (routeId != null) viewModel.loadRouteDetail(routeId) }
    
    LaunchedEffect(Unit) {
        viewModel.startLocationTracking()
        viewModel.fetchCurrentLocation()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    LaunchedEffect(detailState) {
        if (routeId != null && detailState is RouteDetailState.Success) {
            val data = detailState as RouteDetailState.Success
            nombre = data.route.nombre
            fecha = data.route.fechaPlanificada
            notas = data.route.notas ?: ""
            paradas.clear()
            paradas.addAll(data.stops.map { 
                StopRequestDto(it.direccion, it.destinatario, it.telefonoDestinatario, it.latitud, it.longitud, it.notas)
            })
        }
    }

    val lastStopLocation = remember(paradas.size) {
        if (paradas.isNotEmpty()) {
            val last = paradas.last()
            last.latitud to last.longitud
        } else null
    }

    // Diálogos con soporte para tema
    if (stopToDeleteIndex != null) {
        AlertDialog(
            onDismissRequest = { stopToDeleteIndex = null },
            containerColor = surfaceColor,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = stringResource(R.string.dialog_delete_stop_title),
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurfaceColor
                )
            },
            text = { Text(stringResource(R.string.dialog_delete_stop_desc), fontFamily = SpaceGroteskFamily, color = onSurfaceColor) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        paradas.removeAt(stopToDeleteIndex!!)
                        stopToDeleteIndex = null
                    },
                    color = SafetyOrange, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_delete), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { stopToDeleteIndex = null }) {
                    Text(stringResource(R.string.btn_cancel), color = onSurfaceColor, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (phoneToCall != null) {
        AlertDialog(
            onDismissRequest = { phoneToCall = null },
            containerColor = surfaceColor,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.dialog_call_title).split("%")[0])
                        withStyle(style = SpanStyle(color = SafetyOrange)) { append(phoneToCall!!) }
                        append("?")
                    },
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurfaceColor
                )
            },
            text = { Text(stringResource(R.string.dialog_call_desc), fontFamily = SpaceGroteskFamily, color = onSurfaceColor) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                        context.startActivity(intent)
                        phoneToCall = null
                    },
                    color = NeonGreen, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_call), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneToCall = null }) {
                    Text(stringResource(R.string.btn_cancel), color = onSurfaceColor, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (mostrarPlacePicker) {
        PlacePickerScreen(
            lastStopLocation = lastStopLocation,
            existingStops = paradas.map { it.toRouteMapPoint() },
            onLocationConfirmed = { suggestion ->
                stopDireccion = suggestion.displayName
                stopLat = suggestion.latitude.toString()
                stopLon = suggestion.longitude.toString()
                mostrarPlacePicker = false
                viewModel.clearAddressSuggestions()
                viewModel.clearPickedLocation()
            },
            onBack = { 
                mostrarPlacePicker = false 
                viewModel.clearAddressSuggestions()
                viewModel.clearPickedLocation()
            },
            onCallRequest = { phoneToCall = it },
            viewModel = viewModel
        )
        return
    }

    LaunchedEffect(createState) {
        if (createState is CreateRouteState.Success) {
            viewModel.resetCreateState()
            viewModel.clearAddressSuggestions()
            onRouteCreated()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDateSelected = { millis ->
                if (millis != null) {
                    try {
                        val selectedDate = LocalDate.ofEpochDay(millis / 86400000L)
                        val today = LocalDate.now()
                        if (selectedDate.isBefore(today)) {
                            fechaError = context.getString(R.string.error_past_date)
                        } else {
                            fecha = selectedDate.format(dateFormatter)
                            fechaError = ""
                        }
                    } catch (e: Exception) { fechaError = context.getString(R.string.error_invalid_date) }
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header (Siempre arriba)
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp).zIndex(2f),
                color = surfaceColor, shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().drawBehind { drawLine(onSurfaceColor, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = onSurfaceColor, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (routeId == null) stringResource(R.string.title_new_route) else stringResource(R.string.title_edit_route),
                        fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = onSurfaceColor
                    )
                }
            }

            // Contenido adaptable
            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Mapa a la izquierda en Landscape
                    val pendingStopPoint = if (stopLat.isNotBlank() && stopLon.isNotBlank()) {
                        RouteMapPoint(stopLat.toDouble(), stopLon.toDouble(), stopDireccion, stopDestinatario.ifBlank { "..." })
                    } else null

                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxHeight()
                            .zIndex(1f)
                            .clipToBounds()
                            .drawBehind { drawLine(BrutalistBlack, Offset(size.width, 0f), Offset(size.width, size.height), 2.dp.toPx()) }
                    ) {
                        val allStops = paradas.map { it.toRouteMapPoint() }.toMutableList()
                        pendingStopPoint?.let { allStops.add(it) }

                        RouteMapView(
                            stops = allStops, 
                            userLocation = currentLocation?.takeIf { it.first != 0.0 }, 
                            centerKey = centerTrigger, 
                            onCallRequest = { phoneToCall = it }, 
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = surfaceColor, shape = RectangleShape) {
                                Icon(Icons.Default.MyLocation, null, tint = onSurfaceColor, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }

                    // Formulario a la derecha en Landscape
                    Column(
                        modifier = Modifier.weight(0.6f).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CreateRouteFormContent(
                            routeId = routeId,
                            nombre = nombre,
                            onNombreChange = { nombre = it; nombreError = if (it.isBlank()) context.getString(R.string.error_empty_name) else "" },
                            nombreError = nombreError,
                            fecha = fecha,
                            onFechaClick = { showDatePicker = true },
                            fechaError = fechaError,
                            notas = notas,
                            onNotasChange = { notas = it },
                            paradas = paradas,
                            onDeleteStop = { stopToDeleteIndex = it },
                            mostrarFormParada = mostrarFormParada,
                            onToggleFormParada = { mostrarFormParada = !mostrarFormParada },
                            stopDireccion = stopDireccion,
                            onSearchAddress = { if (hasLocationPermission(context)) { viewModel.fetchCurrentLocation(); mostrarPlacePicker = true } else { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) } },
                            stopDireccionError = (stopDireccion.isEmpty() && stopLatError.isNotEmpty()),
                            stopDestinatario = stopDestinatario,
                            onStopDestinatarioChange = { stopDestinatario = it; stopDestinatarioError = if (it.isBlank()) context.getString(R.string.error_empty_name) else "" },
                            stopDestinatarioError = stopDestinatarioError,
                            stopTelefono = stopTelefono,
                            onStopTelefonoChange = { stopTelefono = it; stopPhoneError = if (it.isBlank()) context.getString(R.string.error_empty_phone) else if (!Validators.isValidSpanishPhone(it)) context.getString(R.string.error_invalid_phone) else "" },
                            stopPhoneError = stopPhoneError,
                            stopLat = stopLat,
                            stopLon = stopLon,
                            stopNotas = stopNotas,
                            onStopNotasChange = { stopNotas = it },
                            onConfirmStop = { 
                                paradas.add(StopRequestDto(stopDireccion, stopDestinatario, stopTelefono, stopLat.toDouble(), stopLon.toDouble(), stopNotas.ifBlank { null }))
                                stopDireccion = ""; stopDestinatario = ""; stopTelefono = ""; stopLat = ""; stopLon = ""; stopNotas = ""; mostrarFormParada = false 
                            },
                            createState = createState,
                            onSurfaceColor = onSurfaceColor,
                            surfaceColor = surfaceColor,
                            onSaveRoute = {
                                if (routeId == null) viewModel.createRoute(CreateRouteRequest(nombre, fecha, repartidorId, paradas.toList(), notas.ifBlank { null }))
                                else viewModel.updateRoute(routeId, UpdateRouteRequest(nombre, fecha, notas.ifBlank { null }, paradas.toList()))
                            }
                        )
                    }
                }
            } else {
                // Layout Vertical para Portrait
                val pendingStopPoint = if (stopLat.isNotBlank() && stopLon.isNotBlank()) {
                    RouteMapPoint(stopLat.toDouble(), stopLon.toDouble(), stopDireccion, stopDestinatario.ifBlank { "..." })
                } else null

                // Mapa siempre visible en Portrait
                Box(
                    modifier = Modifier.fillMaxWidth().height(240.dp).zIndex(1f).clipToBounds().drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }
                ) {
                    val allStops = paradas.map { it.toRouteMapPoint() }.toMutableList()
                    pendingStopPoint?.let { allStops.add(it) }

                    RouteMapView(
                        stops = allStops, 
                        userLocation = if (currentLocation?.first != 0.0) currentLocation else null,
                        centerKey = centerTrigger, 
                        onCallRequest = { phoneToCall = it }, 
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                        Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = surfaceColor, shape = RectangleShape) {
                            Icon(Icons.Default.MyLocation, null, tint = onSurfaceColor, modifier = Modifier.padding(8.dp))
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CreateRouteFormContent(
                        routeId = routeId,
                        nombre = nombre,
                        onNombreChange = { nombre = it; nombreError = if (it.isBlank()) context.getString(R.string.error_empty_name) else "" },
                        nombreError = nombreError,
                        fecha = fecha,
                        onFechaClick = { showDatePicker = true },
                        fechaError = fechaError,
                        notas = notas,
                        onNotasChange = { notas = it },
                        paradas = paradas,
                        onDeleteStop = { stopToDeleteIndex = it },
                        mostrarFormParada = mostrarFormParada,
                        onToggleFormParada = { mostrarFormParada = !mostrarFormParada },
                        stopDireccion = stopDireccion,
                        onSearchAddress = { if (hasLocationPermission(context)) { viewModel.fetchCurrentLocation(); mostrarPlacePicker = true } else { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) } },
                        stopDireccionError = (stopDireccion.isEmpty() && stopLatError.isNotEmpty()),
                        stopDestinatario = stopDestinatario,
                        onStopDestinatarioChange = { stopDestinatario = it; stopDestinatarioError = if (it.isBlank()) context.getString(R.string.error_empty_name) else "" },
                        stopDestinatarioError = stopDestinatarioError,
                        stopTelefono = stopTelefono,
                        onStopTelefonoChange = { stopTelefono = it; stopPhoneError = if (it.isBlank()) context.getString(R.string.error_empty_phone) else if (!Validators.isValidSpanishPhone(it)) context.getString(R.string.error_invalid_phone) else "" },
                        stopPhoneError = stopPhoneError,
                        stopLat = stopLat,
                        stopLon = stopLon,
                        stopNotas = stopNotas,
                        onStopNotasChange = { stopNotas = it },
                        onConfirmStop = { 
                            paradas.add(StopRequestDto(stopDireccion, stopDestinatario, stopTelefono, stopLat.toDouble(), stopLon.toDouble(), stopNotas.ifBlank { null }))
                            stopDireccion = ""; stopDestinatario = ""; stopTelefono = ""; stopLat = ""; stopLon = ""; stopNotas = ""; mostrarFormParada = false 
                        },
                        createState = createState,
                        onSurfaceColor = onSurfaceColor,
                        surfaceColor = surfaceColor,
                        onSaveRoute = {
                            if (routeId == null) viewModel.createRoute(CreateRouteRequest(nombre, fecha, repartidorId, paradas.toList(), notas.ifBlank { null }))
                            else viewModel.updateRoute(routeId, UpdateRouteRequest(nombre, fecha, notas.ifBlank { null }, paradas.toList()))
                        }
                    )
                }
            }
        }

        if (routeId != null) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp).zIndex(3f)) {
                Surface(modifier = Modifier.size(48.dp).hardShadow(2.dp, 2.dp).border(2.dp, BrutalistBlack).clickable { viewModel.loadRouteDetail(routeId); authViewModel.checkServerConnectivity() }, color = surfaceColor, shape = MaterialTheme.shapes.extraSmall) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = onSurfaceColor, strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, stringResource(R.string.status_verifying), tint = onSurfaceColor)
                    }
                }
            }
        }
    }
}


@Composable
private fun CreateRouteFormContent(
    routeId: String?,
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreError: String,
    fecha: String,
    onFechaClick: () -> Unit,
    fechaError: String,
    notas: String,
    onNotasChange: (String) -> Unit,
    paradas: List<StopRequestDto>,
    onDeleteStop: (Int) -> Unit,
    mostrarFormParada: Boolean,
    onToggleFormParada: () -> Unit,
    stopDireccion: String,
    onSearchAddress: () -> Unit,
    stopDireccionError: Boolean,
    stopDestinatario: String,
    onStopDestinatarioChange: (String) -> Unit,
    stopDestinatarioError: String,
    stopTelefono: String,
    onStopTelefonoChange: (String) -> Unit,
    stopPhoneError: String,
    stopLat: String,
    stopLon: String,
    stopNotas: String,
    onStopNotasChange: (String) -> Unit,
    onConfirmStop: () -> Unit,
    createState: CreateRouteState,
    onSurfaceColor: Color,
    surfaceColor: Color,
    onSaveRoute: () -> Unit
) {
    SectionLabel(if (routeId == null) stringResource(R.string.label_route_data) else stringResource(R.string.label_edit_data), onSurfaceColor)

    BrutalistField(
        label = stringResource(R.string.label_route_name), value = nombre, required = true,
        onValueChange = onNombreChange,
        placeholder = stringResource(R.string.placeholder_route_name), errorMessage = nombreError,
        onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor
    )

    Column {
        Text(
            text = buildAnnotatedString { append(stringResource(R.string.label_planned_date)); withStyle(style = SpanStyle(color = SafetyOrange)) { append(" *") } },
            fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, color = if (fechaError.isNotEmpty()) SafetyOrange else onSurfaceColor, modifier = Modifier.padding(bottom = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().hardShadow().border(2.dp, if (fechaError.isNotEmpty()) SafetyOrange else BrutalistBlack).background(surfaceColor).clickable { onFechaClick() }.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = if (fecha.isEmpty()) stringResource(R.string.placeholder_date) else fecha, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (fecha.isEmpty()) onSurfaceColor.copy(alpha = 0.4f) else onSurfaceColor)
            Text(text = "📅", fontSize = 20.sp)
        }
        if (fechaError.isNotEmpty()) { Text(text = fechaError, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SafetyOrange, modifier = Modifier.padding(top = 4.dp)) }
    }

    BrutalistField(label = stringResource(R.string.label_notes_optional), value = notas, onValueChange = onNotasChange, placeholder = stringResource(R.string.placeholder_notes), onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)

    if (paradas.isNotEmpty()) {
        SectionLabel(stringResource(R.string.label_stops_count, paradas.size), onSurfaceColor)
        paradas.forEachIndexed { index, parada -> ParadaRow(index = index + 1, parada = parada, onDelete = { onDeleteStop(index) }, onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor) }
    }

    Row(
        modifier = Modifier.fillMaxWidth().hardShadow().border(2.dp, BrutalistBlack).background(if (mostrarFormParada) onSurfaceColor.copy(alpha = 0.05f) else surfaceColor).clickable { onToggleFormParada() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Add, null, tint = onSurfaceColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(if (mostrarFormParada) stringResource(R.string.btn_cancel_stop) else stringResource(R.string.btn_add_stop), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp, color = onSurfaceColor)
    }

    if (mostrarFormParada) {
        Column(
            modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack).background(onSurfaceColor.copy(alpha = 0.05f)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.label_new_stop), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 2.sp, color = onSurfaceColor)
            Column {
                Text(text = buildAnnotatedString { append(stringResource(R.string.label_address)); withStyle(style = SpanStyle(color = SafetyOrange)) { append(" *") } }, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, color = onSurfaceColor, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().border(2.dp, if (stopDireccionError) SafetyOrange else BrutalistBlack).background(surfaceColor)
                        .clickable { onSearchAddress() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = if (stopDireccion.isEmpty()) stringResource(R.string.placeholder_stop_address) else stopDireccion, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (stopDireccion.isEmpty()) onSurfaceColor.copy(alpha = 0.4f) else onSurfaceColor, modifier = Modifier.weight(1f), maxLines = 2)
                    Icon(Icons.Default.Search, null, tint = onSurfaceColor)
                }
            }
            BrutalistField(label = stringResource(R.string.label_recipient), value = stopDestinatario, required = true, onValueChange = onStopDestinatarioChange, placeholder = stringResource(R.string.placeholder_recipient), errorMessage = stopDestinatarioError, onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)
            BrutalistField(label = stringResource(R.string.label_phone), value = stopTelefono, required = true, onValueChange = onStopTelefonoChange, placeholder = stringResource(R.string.placeholder_phone), keyboardType = KeyboardType.Phone, errorMessage = stopPhoneError, onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrutalistField(label = stringResource(R.string.label_latitude), value = stopLat, required = true, onValueChange = { }, placeholder = "0.0", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f), readOnly = true, onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)
                BrutalistField(label = stringResource(R.string.label_longitude), value = stopLon, required = true, onValueChange = { }, placeholder = "0.0", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f), readOnly = true, onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)
            }
            BrutalistField(label = stringResource(R.string.label_stop_notes_optional), value = stopNotas, onValueChange = onStopNotasChange, placeholder = stringResource(R.string.placeholder_notes_stop), onSurfaceColor = onSurfaceColor, surfaceColor = surfaceColor)
            val canAddStop = stopDireccion.isNotBlank() && stopDestinatario.isNotBlank() && stopTelefono.isNotBlank() && stopLat.isNotBlank() && stopLon.isNotBlank()
            Row(
                modifier = Modifier.fillMaxWidth().hardShadow(color = NeonGreen).border(2.dp, BrutalistBlack).background(if (canAddStop) NeonGreen else onSurfaceColor.copy(alpha = 0.2f)).clickable(enabled = canAddStop) { if (canAddStop) onConfirmStop() }.padding(16.dp),
                horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.btn_confirm_stop), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp, color = if (canAddStop) Color.Black else onSurfaceColor.copy(alpha = 0.5f))
            }
        }
    }

    if (createState is CreateRouteState.Error) {
        Box(modifier = Modifier.fillMaxWidth().border(2.dp, SafetyOrange).background(SafetyOrange.copy(alpha = 0.1f)).padding(12.dp)) {
            Text((createState as CreateRouteState.Error).message, fontFamily = SpaceGroteskFamily, fontSize = 13.sp, color = SafetyOrange)
        }
    }

    Spacer(Modifier.height(8.dp))
    val canSave = nombre.isNotBlank() && fecha.isNotBlank() && paradas.isNotEmpty() && createState !is CreateRouteState.Loading
    Row(
        modifier = Modifier.fillMaxWidth().hardShadow(6.dp, 6.dp, if (canSave) SafetyOrange else Color.Gray).border(2.dp, BrutalistBlack).background(if (canSave) onSurfaceColor else onSurfaceColor.copy(alpha = 0.4f)).clickable(enabled = canSave) { if (canSave) onSaveRoute() }.height(72.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
    ) {
        if (createState is CreateRouteState.Loading) CircularProgressIndicator(color = surfaceColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        else Text(if (routeId == null) stringResource(R.string.btn_save_route) else stringResource(R.string.btn_update_route), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp, color = if (canSave) surfaceColor else onSurfaceColor.copy(alpha = 0.5f))
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun SectionLabel(text: String, onSurfaceColor: Color) {
    Text(text = text, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp, color = onSurfaceColor, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun BrutalistField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier, errorMessage: String = "", readOnly: Boolean = false, required: Boolean = false, onSurfaceColor: Color, surfaceColor: Color) {
    Column(modifier = modifier) {
        Text(text = buildAnnotatedString { append(label); if (required) withStyle(style = SpanStyle(color = SafetyOrange)) { append(" *") } }, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, color = if (errorMessage.isNotEmpty()) SafetyOrange else onSurfaceColor, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(
            value = value, 
            onValueChange = onValueChange, 
            singleLine = true, 
            readOnly = readOnly, 
            textStyle = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (readOnly) onSurfaceColor.copy(alpha = 0.5f) else onSurfaceColor), 
            cursorBrush = SolidColor(NeonGreen), 
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType), 
            modifier = Modifier.fillMaxWidth().border(2.dp, if (errorMessage.isNotEmpty()) SafetyOrange else BrutalistBlack).background(if (readOnly) onSurfaceColor.copy(alpha = 0.05f) else surfaceColor).height(56.dp), 
            decorationBox = { innerTextField -> Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) { if (value.isEmpty()) Text(text = placeholder, fontFamily = SpaceGroteskFamily, fontSize = 16.sp, color = onSurfaceColor.copy(alpha = 0.4f)); innerTextField() } }
        )
        if (errorMessage.isNotEmpty()) Text(text = errorMessage, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SafetyOrange, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ParadaRow(index: Int, parada: StopRequestDto, onDelete: () -> Unit, onSurfaceColor: Color, surfaceColor: Color) {
    Row(modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack).background(surfaceColor).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(32.dp).border(2.dp, BrutalistBlack).background(onSurfaceColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Text(text = "$index", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, color = onSurfaceColor) }
        Column(modifier = Modifier.weight(1f)) { Text(text = parada.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = onSurfaceColor, maxLines = 1); Text(text = parada.direccion, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.6f), maxLines = 1) }
        Box(modifier = Modifier.size(36.dp).clickable { onDelete() }, contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.btn_delete), tint = SafetyOrange, modifier = Modifier.size(20.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val todayUtc = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
                return utcTimeMillis >= todayUtc
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss, 
        confirmButton = { TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) { Text(stringResource(R.string.btn_ok), fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurface) } }, 
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_cancel), fontFamily = SpaceGroteskFamily, color = SafetyOrange) } }
    ) { 
        DatePicker(state = datePickerState) 
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
