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
import androidx.compose.ui.platform.LocalContext
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
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.StopRequestDto
import com.slior.data.remote.dto.UpdateRouteRequest
import com.slior.ui.components.PlacePickerScreen
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.map.toRouteMapPoint
import com.slior.ui.theme.*
import com.slior.util.Result
import com.slior.util.Validators
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CreateRouteScreen(
    repartidorId: String,
    routeId: String? = null, // null = crear, !null = editar
    onBack: () -> Unit,
    onRouteCreated: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: com.slior.viewmodel.AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val detailState by viewModel.detailState.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    
    val isRefreshing = detailState is RouteDetailState.Loading && routeId != null
    var centerTrigger by remember { mutableStateOf(0) }
    var phoneToCall by remember { mutableStateOf<String?>(null) }

    var nombre        by rememberSaveable { mutableStateOf("") }
    var fecha         by rememberSaveable { mutableStateOf("") }
    var notas         by rememberSaveable { mutableStateOf("") }
    val paradas       = remember { mutableStateListOf<StopRequestDto>() }

    // Campos de la parada en construcción
    var stopDireccion    by rememberSaveable { mutableStateOf("") }
    var stopDestinatario by rememberSaveable { mutableStateOf("") }
    var stopTelefono     by rememberSaveable { mutableStateOf("") }
    var stopLat          by rememberSaveable { mutableStateOf("") }
    var stopLon          by rememberSaveable { mutableStateOf("") }
    var stopNotas        by rememberSaveable { mutableStateOf("") }

    // Control de visibilidad del selector de mapa
    var mostrarPlacePicker by rememberSaveable { mutableStateOf(false) }

    // Mostrar/ocultar formulario de parada
    var mostrarFormParada by rememberSaveable { mutableStateOf(false) }

    // Errores de validación
    var stopPhoneError   by rememberSaveable { mutableStateOf("") }
    var stopLatError     by rememberSaveable { mutableStateOf("") }
    var stopLonError     by rememberSaveable { mutableStateOf("") }
    var stopDestinatarioError by rememberSaveable { mutableStateOf("") }
    var fechaError       by rememberSaveable { mutableStateOf("") }
    var nombreError      by rememberSaveable { mutableStateOf("") }

    // DatePicker state
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
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

    // Cargar datos si estamos en modo edición
    LaunchedEffect(routeId) {
        if (routeId != null) {
            viewModel.loadRouteDetail(routeId)
        }
    }

    // Rellenar formulario cuando el detalle carga exitosamente
    LaunchedEffect(detailState) {
        if (routeId != null && detailState is RouteDetailState.Success) {
            val data = detailState as RouteDetailState.Success
            nombre = data.route.nombre
            fecha = data.route.fechaPlanificada
            notas = data.route.notas ?: ""
            paradas.clear()
            paradas.addAll(data.stops.map { 
                StopRequestDto(
                    direccion = it.direccion,
                    destinatario = it.destinatario,
                    telefonoDestinatario = it.telefonoDestinatario,
                    latitud = it.latitud,
                    longitud = it.longitud,
                    notas = it.notas
                )
            })
        }
    }

    // Ubicación de la última parada para centrar el mapa
    val lastStopLocation = remember(paradas.size) {
        if (paradas.isNotEmpty()) {
            val last = paradas.last()
            last.latitud to last.longitud
        } else null
    }

    // --- POPUP BRUTALISTA DE CONFIRMACIÓN DE LLAMADA ---
    if (phoneToCall != null) {
        AlertDialog(
            onDismissRequest = { phoneToCall = null },
            containerColor = BrutalistWhite,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = buildAnnotatedString {
                        append("¿LLAMAR A ")
                        withStyle(style = SpanStyle(color = SafetyOrange)) {
                            append(phoneToCall!!)
                        }
                        append("?")
                    },
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = BrutalistBlack
                )
            },
            text = {
                Text(
                    "Se abrirá la aplicación de teléfono del dispositivo.",
                    fontFamily = SpaceGroteskFamily,
                    color = BrutalistBlack
                )
            },
            confirmButton = {
                Surface(
                    modifier = Modifier
                        .border(2.dp, BrutalistBlack)
                        .clickable { 
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                            context.startActivity(intent)
                            phoneToCall = null
                        },
                    color = NeonGreen,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(
                        text = "LLAMAR",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = BrutalistBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneToCall = null }) {
                    Text("CANCELAR", color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
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

    Box(modifier = Modifier.fillMaxSize().background(BrutalistWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── 1. TopAppBar (FIJA) ─────────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .zIndex(2f),
                color = BrutalistWhite,
                shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                        }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(48.dp).clickable { onBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = BrutalistBlack, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (routeId == null) "NUEVA RUTA" else "EDITAR RUTA",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = BrutalistBlack
                    )
                }
            }

            // ── 2. Zona de Mapa (FIJA) ──────────────────────────────────────
            if (paradas.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .zIndex(1f)
                        .clipToBounds()
                        .drawBehind {
                            drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
                        }
                ) {
                    RouteMapView(
                        stops = paradas.map { it.toRouteMapPoint() },
                        userLocation = currentLocation,
                        centerKey = centerTrigger,
                        onCallRequest = { phoneToCall = it },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Botón de centrado
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .border(2.dp, BrutalistBlack)
                                .clickable { centerTrigger++ },
                            color = BrutalistWhite,
                            shape = RectangleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Centrar",
                                tint = BrutalistBlack,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }

            // ── 3. Zona de Formulario (SCROLL INDEPENDIENTE) ───────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .zIndex(0f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SectionLabel(if (routeId == null) "DATOS DE LA RUTA" else "EDITAR DATOS")

                BrutalistField(
                    label       = "NOMBRE",
                    value       = nombre,
                    required    = true,
                    onValueChange = { 
                        nombre = it
                        nombreError = if (it.isBlank()) "El nombre es obligatorio" 
                                     else if (!Validators.isValidFieldLength(it, 100)) "Máximo 100 caracteres"
                                     else ""
                    },
                    placeholder = "EJ. RUTA NORTE",
                    errorMessage = nombreError
                )

                if (showDatePicker) {
                    DatePickerDialog(
                        onDateSelected = { millis ->
                            if (millis != null) {
                                try {
                                    val selectedDate = LocalDate.ofEpochDay(millis / 86400000L)
                                    fecha = selectedDate.format(dateFormatter)
                                    fechaError = ""
                                } catch (e: Exception) {
                                    fechaError = "Error al seleccionar la fecha"
                                }
                            }
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }

                Column {
                    Text(
                        text = buildAnnotatedString {
                            append("FECHA (YYYY-MM-DD)")
                            withStyle(style = SpanStyle(color = SafetyOrange)) {
                                append(" *")
                            }
                        },
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 11.sp,
                        letterSpacing = 1.5.sp,
                        color         = if (fechaError.isNotEmpty()) SafetyOrange else BrutalistBlack,
                        modifier      = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow()
                            .border(2.dp, if (fechaError.isNotEmpty()) SafetyOrange else BrutalistBlack)
                            .background(BrutalistWhite)
                            .clickable { showDatePicker = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text       = if (fecha.isEmpty()) "Selecciona una fecha" else fecha,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 16.sp,
                            color      = if (fecha.isEmpty()) Color(0xFFB0B0B0) else BrutalistBlack
                        )
                        Text(text = "📅", fontSize = 20.sp)
                    }
                    if (fechaError.isNotEmpty()) {
                        Text(text = fechaError, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SafetyOrange, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                BrutalistField(
                    label         = "NOTAS (OPCIONAL)",
                    value         = notas,
                    onValueChange = { notas = it },
                    placeholder   = "Observaciones..."
                )

                if (paradas.isNotEmpty()) {
                    SectionLabel("PARADAS (${paradas.size})")
                    paradas.forEachIndexed { index, parada ->
                        ParadaRow(index = index + 1, parada = parada, onDelete = { paradas.removeAt(index) })
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hardShadow()
                        .border(2.dp, BrutalistBlack)
                        .background(if (mostrarFormParada) Color(0xFFF4F4F5) else BrutalistWhite)
                        .clickable { mostrarFormParada = !mostrarFormParada }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = BrutalistBlack, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (mostrarFormParada) "CANCELAR PARADA" else "AÑADIR PARADA", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.sp, color = BrutalistBlack)
                }

                if (mostrarFormParada) {
                    Column(
                        modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack).background(Color(0xFFF4F4F5)).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("NUEVA PARADA", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 13.sp, letterSpacing = 2.sp, color = BrutalistBlack)
                        Column {
                            Text(text = buildAnnotatedString { append("DIRECCIÓN"); withStyle(style = SpanStyle(color = SafetyOrange)) { append(" *") } }, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, color = BrutalistBlack, modifier = Modifier.padding(bottom = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().border(2.dp, if (stopDireccion.isEmpty() && stopLatError.isNotEmpty()) SafetyOrange else BrutalistBlack).background(BrutalistWhite)
                                    .clickable { if (hasLocationPermission(context)) { viewModel.fetchCurrentLocation(); mostrarPlacePicker = true } else { permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) } }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = if (stopDireccion.isEmpty()) "Toca para buscar o seleccionar en mapa..." else stopDireccion, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (stopDireccion.isEmpty()) Color(0xFFB0B0B0) else BrutalistBlack, modifier = Modifier.weight(1f), maxLines = 2)
                                Icon(Icons.Default.Search, null, tint = BrutalistBlack)
                            }
                        }
                        BrutalistField(label = "DESTINATARIO", value = stopDestinatario, required = true, onValueChange = { stopDestinatario = it; stopDestinatarioError = if (it.isBlank()) "El nombre es obligatorio" else "" }, placeholder = "Nombre del destinatario", errorMessage = stopDestinatarioError)
                        BrutalistField(label = "TELÉFONO", value = stopTelefono, required = true, onValueChange = { stopTelefono = it; stopPhoneError = if (it.isBlank()) "El teléfono es obligatorio" else if (!Validators.isValidSpanishPhone(it)) "Teléfono inválido" else "" }, placeholder = "600 000 000", keyboardType = KeyboardType.Phone, errorMessage = stopPhoneError)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BrutalistField(label = "LATITUD", value = stopLat, required = true, onValueChange = { stopLat = it }, placeholder = "0.0", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f), errorMessage = stopLatError, readOnly = true)
                            BrutalistField(label = "LONGITUD", value = stopLon, required = true, onValueChange = { stopLon = it }, placeholder = "0.0", keyboardType = KeyboardType.Decimal, modifier = Modifier.weight(1f), errorMessage = stopLonError, readOnly = true)
                        }
                        BrutalistField(label = "NOTAS PARADA (OPCIONAL)", value = stopNotas, onValueChange = { stopNotas = it }, placeholder = "Dejar en portería...")
                        val canAddStop = stopDireccion.isNotBlank() && stopDestinatario.isNotBlank() && stopTelefono.isNotBlank() && stopLat.isNotBlank() && stopLon.isNotBlank()
                        Row(
                            modifier = Modifier.fillMaxWidth().hardShadow(color = NeonGreen).border(2.dp, BrutalistBlack).background(if (canAddStop) NeonGreen else Color(0xFFD4D4D8))
                                .clickable(enabled = canAddStop) { if (canAddStop) { paradas.add(StopRequestDto(stopDireccion, stopDestinatario, stopTelefono, stopLat.toDouble(), stopLon.toDouble(), stopNotas.ifBlank { null })); stopDireccion = ""; stopDestinatario = ""; stopTelefono = ""; stopLat = ""; stopLon = ""; stopNotas = ""; mostrarFormParada = false } }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✓  CONFIRMAR PARADA", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, letterSpacing = 1.sp, color = if (canAddStop) BrutalistBlack else Color(0xFF71717A))
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
                    modifier = Modifier.fillMaxWidth().hardShadow(6.dp, 6.dp, if (canSave) SafetyOrange else Color.Gray).border(2.dp, BrutalistBlack).background(if (canSave) BrutalistBlack else Color(0xFFB0B0B0))
                        .clickable(enabled = canSave) {
                            if (canSave) {
                                if (routeId == null) viewModel.createRoute(CreateRouteRequest(nombre, fecha, repartidorId, paradas.toList(), notas.ifBlank { null }))
                                else viewModel.updateRoute(routeId, UpdateRouteRequest(nombre, fecha, notas.ifBlank { null }, paradas.toList()))
                            }
                        }.height(72.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                ) {
                    if (createState is CreateRouteState.Loading) CircularProgressIndicator(color = BrutalistWhite, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    else Text(if (routeId == null) "GUARDAR RUTA" else "ACTUALIZAR RUTA", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp, color = if (canSave) BrutalistWhite else Color(0xFF9E9E9E))
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Botón flotante de refresco ─────────────────────
        if (routeId != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 80.dp, end = 16.dp)
                    .zIndex(3f)
            ) {
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .hardShadow(2.dp, 2.dp)
                        .border(2.dp, BrutalistBlack)
                        .clickable { 
                            viewModel.loadRouteDetail(routeId)
                            authViewModel.checkServerConnectivity()
                        },
                    color = BrutalistWhite,
                    shape = MaterialTheme.shapes.extraSmall
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrutalistBlack, strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, "Refrescar", tint = BrutalistBlack)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text = text, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp, color = BrutalistBlack, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun BrutalistField(label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, keyboardType: KeyboardType = KeyboardType.Text, modifier: Modifier = Modifier, errorMessage: String = "", readOnly: Boolean = false, required: Boolean = false) {
    Column(modifier = modifier) {
        Text(text = buildAnnotatedString { append(label); if (required) withStyle(style = SpanStyle(color = SafetyOrange)) { append(" *") } }, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 1.5.sp, color = if (errorMessage.isNotEmpty()) SafetyOrange else BrutalistBlack, modifier = Modifier.padding(bottom = 4.dp))
        BasicTextField(value = value, onValueChange = onValueChange, singleLine = true, readOnly = readOnly, textStyle = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (readOnly) Color(0xFF71717A) else BrutalistBlack), cursorBrush = SolidColor(NeonGreen), keyboardOptions = KeyboardOptions(keyboardType = keyboardType), modifier = Modifier.fillMaxWidth().border(2.dp, if (errorMessage.isNotEmpty()) SafetyOrange else BrutalistBlack).background(if (readOnly) Color(0xFFF4F4F5) else BrutalistWhite).height(56.dp), decorationBox = { innerTextField -> Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) { if (value.isEmpty()) Text(text = placeholder, fontFamily = SpaceGroteskFamily, fontSize = 16.sp, color = Color(0xFFB0B0B0)); innerTextField() } })
        if (errorMessage.isNotEmpty()) Text(text = errorMessage, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SafetyOrange, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ParadaRow(index: Int, parada: StopRequestDto, onDelete: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack).background(BrutalistWhite).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(modifier = Modifier.size(32.dp).border(2.dp, BrutalistBlack).background(Color(0xFFF4F4F5)), contentAlignment = Alignment.Center) { Text(text = "$index", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrutalistBlack) }
        Column(modifier = Modifier.weight(1f)) { Text(text = parada.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrutalistBlack, maxLines = 1); Text(text = parada.direccion, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = Color(0xFF71717A), maxLines = 1) }
        Box(modifier = Modifier.size(36.dp).clickable { onDelete() }, contentAlignment = Alignment.Center) { Icon(imageVector = Icons.Default.Delete, contentDescription = "Eliminar parada", tint = SafetyOrange, modifier = Modifier.size(20.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(onDateSelected: (Long?) -> Unit, onDismiss: () -> Unit) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(onDismissRequest = onDismiss, confirmButton = { TextButton(onClick = { onDateSelected(datePickerState.selectedDateMillis) }) { Text("OK", fontFamily = SpaceGroteskFamily, color = BrutalistBlack) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR", fontFamily = SpaceGroteskFamily, color = SafetyOrange) } }) { DatePicker(state = datePickerState) }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}
