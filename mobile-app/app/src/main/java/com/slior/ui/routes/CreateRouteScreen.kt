package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.StopRequestDto
import com.slior.ui.components.SliorDesignTokens
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*
import com.slior.util.Validators
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun CreateRouteScreen(
    repartidorId: String,
    onBack: () -> Unit,
    onRouteCreated: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val createState        by viewModel.createState.collectAsStateWithLifecycle()
    val addressSuggestions by viewModel.addressSuggestions.collectAsStateWithLifecycle()
    val isSearching        by viewModel.isSearchingAddresses.collectAsStateWithLifecycle()

    var nombre        by remember { mutableStateOf("") }
    var fecha         by remember { mutableStateOf("") }
    var notas         by remember { mutableStateOf("") }
    val paradas       = remember { mutableStateListOf<StopRequestDto>() }

    // Campos de la parada en construcción
    var stopDireccion    by remember { mutableStateOf("") }
    var stopDestinatario by remember { mutableStateOf("") }
    var stopTelefono     by remember { mutableStateOf("") }
    var stopLat          by remember { mutableStateOf("") }
    var stopLon          by remember { mutableStateOf("") }
    var stopNotas        by remember { mutableStateOf("") }

    // Errores de validación
    var stopPhoneError   by remember { mutableStateOf("") }
    var stopLatError     by remember { mutableStateOf("") }
    var stopLonError     by remember { mutableStateOf("") }
    var stopDestinatarioError by remember { mutableStateOf("") }
    var fechaError       by remember { mutableStateOf("") }
    var nombreError      by remember { mutableStateOf("") }

    // DatePicker state
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    // Mostrar/ocultar formulario de parada
    var mostrarFormParada by remember { mutableStateOf(false) }

    LaunchedEffect(createState) {
        if (createState is CreateRouteState.Success) {
            viewModel.resetCreateState()
            viewModel.clearAddressSuggestions()
            onRouteCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
        // ── TopAppBar ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistWhite)
                .drawBehind {
                    drawLine(
                        color       = BrutalistBlack,
                        start       = Offset(0f, size.height),
                        end         = Offset(size.width, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(horizontal = 8.dp)
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint               = BrutalistBlack,
                    modifier           = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "NUEVA RUTA",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = BrutalistBlack
            )
        }

        // ── Cuerpo scrollable ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Sección: Datos de la ruta ──────────────────────────────
            SectionLabel("DATOS DE LA RUTA")

            BrutalistField(
                label       = "NOMBRE",
                value       = nombre,
                onValueChange = { 
                    nombre = it
                    nombreError = ""
                },
                placeholder = "EJ. RUTA NORTE",
                errorMessage = nombreError
            )

            // DatePicker para la fecha
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
                    text          = "FECHA (YYYY-MM-DD)",
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
                    Text(
                        text       = "📅",
                        fontSize   = 20.sp
                    )
                }
                if (fechaError.isNotEmpty()) {
                    Text(
                        text       = fechaError,
                        fontFamily = SpaceGroteskFamily,
                        fontSize   = 11.sp,
                        color      = SafetyOrange,
                        modifier   = Modifier.padding(top = 4.dp)
                    )
                }
            }

            BrutalistField(
                label         = "NOTAS (OPCIONAL)",
                value         = notas,
                onValueChange = { notas = it },
                placeholder   = "Observaciones..."
            )

            // ── Sección: Paradas añadidas ──────────────────────────────
            if (paradas.isNotEmpty()) {
                SectionLabel("PARADAS (${paradas.size})")
                paradas.forEachIndexed { index, parada ->
                    ParadaRow(
                        index  = index + 1,
                        parada = parada,
                        onDelete = { paradas.removeAt(index) }
                    )
                }
            }

            // ── Botón añadir parada ────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
                    .border(2.dp, BrutalistBlack)
                    .background(if (mostrarFormParada) Color(0xFFF4F4F5) else BrutalistWhite)
                    .clickable { mostrarFormParada = !mostrarFormParada }
                    .padding(16.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = null,
                    tint               = BrutalistBlack,
                    modifier           = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text          = if (mostrarFormParada) "CANCELAR PARADA" else "AÑADIR PARADA",
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 14.sp,
                    letterSpacing = 1.sp,
                    color         = BrutalistBlack
                )
            }

            // ── Formulario de nueva parada ─────────────────────────────
            if (mostrarFormParada) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, BrutalistBlack)
                        .background(Color(0xFFF4F4F5))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text          = "NUEVA PARADA",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 13.sp,
                        letterSpacing = 2.sp,
                        color         = BrutalistBlack
                    )

                    // Campo dirección con autocompletado
                    Column {
                        BrutalistField(
                            label         = "DIRECCIÓN",
                            value         = stopDireccion,
                            onValueChange = {
                                stopDireccion = it
                                viewModel.searchAddressSuggestions(it)
                            },
                            placeholder   = "Calle Mayor, Huelva"
                        )

                        // Lista de sugerencias
                        if (isSearching) {
                            Text(
                                text       = "Buscando...",
                                fontFamily = SpaceGroteskFamily,
                                fontSize   = 12.sp,
                                color      = Color(0xFF71717A),
                                modifier   = Modifier.padding(top = 4.dp)
                            )
                        }

                        if (addressSuggestions.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(2.dp, BrutalistBlack)
                                    .background(BrutalistWhite)
                            ) {
                                addressSuggestions.take(5).forEach { suggestion ->
                                    SuggestionRow(
                                        suggestion = suggestion,
                                        onClick    = {
                                            stopDireccion = suggestion.displayName
                                            stopLat       = suggestion.latitude.toString()
                                            stopLon       = suggestion.longitude.toString()
                                            viewModel.clearAddressSuggestions()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    BrutalistField(
                        label         = "DESTINATARIO",
                        value         = stopDestinatario,
                        onValueChange = { 
                            stopDestinatario = it
                            stopDestinatarioError = ""
                        },
                        placeholder   = "Nombre del destinatario",
                        errorMessage = stopDestinatarioError
                    )

                    BrutalistField(
                        label         = "TELÉFONO",
                        value         = stopTelefono,
                        onValueChange = { 
                            stopTelefono = it
                            stopPhoneError = ""
                        },
                        placeholder   = "600 000 000",
                        keyboardType  = KeyboardType.Phone,
                        errorMessage = stopPhoneError
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrutalistField(
                            label         = "LATITUD",
                            value         = stopLat,
                            onValueChange = { 
                                stopLat = it
                                stopLatError = ""
                            },
                            placeholder   = "37.2600",
                            keyboardType  = KeyboardType.Decimal,
                            modifier      = Modifier.weight(1f),
                            errorMessage = stopLatError
                        )
                        BrutalistField(
                            label         = "LONGITUD",
                            value         = stopLon,
                            onValueChange = { 
                                stopLon = it
                                stopLonError = ""
                            },
                            placeholder   = "-6.9500",
                            keyboardType  = KeyboardType.Decimal,
                            modifier      = Modifier.weight(1f),
                            errorMessage = stopLonError
                        )
                    }

                    BrutalistField(
                        label         = "NOTAS PARADA (OPCIONAL)",
                        value         = stopNotas,
                        onValueChange = { stopNotas = it },
                        placeholder   = "Dejar en portería..."
                    )

                    // Validar todos los campos de la parada
                    val canAddStop = stopDireccion.isNotBlank()
                            && stopDestinatario.isNotBlank()
                            && stopTelefono.isNotBlank()
                            && stopLat.isNotBlank()
                            && stopLon.isNotBlank()
                            && Validators.isValidSpanishPhone(stopTelefono)
                            && Validators.isValidLatitude(stopLat)
                            && Validators.isValidLongitude(stopLon)
                            && Validators.isValidFieldLength(stopDestinatario, 100)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow(color = NeonGreen)
                            .border(2.dp, BrutalistBlack)
                            .background(if (canAddStop) NeonGreen else Color(0xFFD4D4D8))
                            .clickable(enabled = canAddStop) {
                                // Validar antes de agregar
                                var hasError = false
                                
                                if (!Validators.isValidSpanishPhone(stopTelefono)) {
                                    stopPhoneError = "Teléfono inválido (ej: 600000000)"
                                    hasError = true
                                }
                                if (!Validators.isValidLatitude(stopLat)) {
                                    stopLatError = "Latitud inválida (-90 a 90)"
                                    hasError = true
                                }
                                if (!Validators.isValidLongitude(stopLon)) {
                                    stopLonError = "Longitud inválida (-180 a 180)"
                                    hasError = true
                                }
                                if (!Validators.isValidFieldLength(stopDestinatario, 100)) {
                                    stopDestinatarioError = "Nombre muy largo (máx. 100 caracteres)"
                                    hasError = true
                                }
                                
                                if (!hasError) {
                                    paradas.add(
                                        StopRequestDto(
                                            direccion            = stopDireccion,
                                            destinatario         = stopDestinatario,
                                            telefonoDestinatario = stopTelefono,
                                            latitud              = stopLat.toDouble(),
                                            longitud             = stopLon.toDouble(),
                                            notas                = stopNotas.ifBlank { null }
                                        )
                                    )
                                    // Resetear campos y errores
                                    stopDireccion      = ""
                                    stopDestinatario   = ""
                                    stopTelefono       = ""
                                    stopLat            = ""
                                    stopLon            = ""
                                    stopNotas          = ""
                                    stopPhoneError     = ""
                                    stopLatError       = ""
                                    stopLonError       = ""
                                    stopDestinatarioError = ""
                                    mostrarFormParada  = false
                                    viewModel.clearAddressSuggestions()
                                }
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            text          = "✓  CONFIRMAR PARADA",
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Black,
                            fontSize      = 14.sp,
                            letterSpacing = 1.sp,
                            color         = BrutalistBlack
                        )
                    }
                }
            }

            // ── Error de creación ──────────────────────────────────────
            if (createState is CreateRouteState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(2.dp, SafetyOrange)
                        .background(SafetyOrange.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(
                        text       = (createState as CreateRouteState.Error).message,
                        fontFamily = SpaceGroteskFamily,
                        fontSize   = 13.sp,
                        color      = SafetyOrange
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Botón guardar ruta ─────────────────────────────────────
            val canSave = nombre.isNotBlank()
                    && fecha.isNotBlank()
                    && paradas.isNotEmpty()
                    && Validators.isValidDate(fecha)
                    && Validators.isValidFieldLength(nombre, 100)
                    && createState !is CreateRouteState.Loading

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow(
                        offsetX = 6.dp,
                        offsetY = 6.dp,
                        color   = SafetyOrange
                    )
                    .border(2.dp, BrutalistBlack)
                    .background(if (canSave) BrutalistBlack else Color(0xFFB0B0B0))
                    .clickable(enabled = canSave) {
                        // Validar antes de enviar
                        var hasError = false
                        
                        if (nombre.isBlank()) {
                            nombreError = "El nombre no puede estar vacío"
                            hasError = true
                        } else if (!Validators.isValidFieldLength(nombre, 100)) {
                            nombreError = "El nombre es demasiado largo (máx. 100 caracteres)"
                            hasError = true
                        }
                        
                        if (fecha.isBlank()) {
                            fechaError = "La fecha no puede estar vacía"
                            hasError = true
                        } else if (!Validators.isValidDate(fecha)) {
                            fechaError = "Fecha inválida (usa YYYY-MM-DD)"
                            hasError = true
                        }
                        
                        if (paradas.isEmpty()) {
                            hasError = true
                        }
                        
                        if (!hasError) {
                            viewModel.createRoute(
                                CreateRouteRequest(
                                    nombre          = nombre,
                                    fechaPlanificada = fecha,
                                    repartidorId    = repartidorId,
                                    paradas         = paradas.toList(),
                                    notas           = notas.ifBlank { null }
                                )
                            )
                        }
                    }
                    .height(72.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (createState is CreateRouteState.Loading) {
                    CircularProgressIndicator(
                        color       = BrutalistWhite,
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text          = "GUARDAR RUTA",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 18.sp,
                        letterSpacing = 2.sp,
                        color         = if (canSave) BrutalistWhite else Color(0xFF9E9E9E)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Componentes privados
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        fontFamily    = SpaceGroteskFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 12.sp,
        letterSpacing = 2.sp,
        color         = Color(0xFF71717A),
        modifier      = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun BrutalistField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier,
    errorMessage: String = ""
) {
    Column(modifier = modifier) {
        Text(
            text          = label,
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Black,
            fontSize      = 11.sp,
            letterSpacing = 1.5.sp,
            color         = if (errorMessage.isNotEmpty()) SafetyOrange else BrutalistBlack,
            modifier      = Modifier.padding(bottom = 4.dp)
        )
        BasicTextField(
            value           = value,
            onValueChange   = onValueChange,
            singleLine      = true,
            textStyle       = TextStyle(
                fontFamily  = SpaceGroteskFamily,
                fontWeight  = FontWeight.Bold,
                fontSize    = 16.sp,
                color       = BrutalistBlack
            ),
            cursorBrush     = SolidColor(NeonGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier        = Modifier
                .fillMaxWidth()
                .border(2.dp, if (errorMessage.isNotEmpty()) SafetyOrange else BrutalistBlack)
                .background(BrutalistWhite)
                .height(56.dp),
            decorationBox   = { innerTextField ->
                Box(
                    modifier         = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text       = placeholder,
                            fontFamily = SpaceGroteskFamily,
                            fontSize   = 16.sp,
                            color      = Color(0xFFB0B0B0)
                        )
                    }
                    innerTextField()
                }
            }
        )
        if (errorMessage.isNotEmpty()) {
            Text(
                text       = errorMessage,
                fontFamily = SpaceGroteskFamily,
                fontSize   = 11.sp,
                color      = SafetyOrange,
                modifier   = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SuggestionRow(
    suggestion: AddressSuggestion,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color       = Color(0xFFE4E4E7),
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text       = suggestion.displayName,
            fontFamily = SpaceGroteskFamily,
            fontSize   = 13.sp,
            color      = BrutalistBlack,
            maxLines   = 2
        )
    }
}

@Composable
private fun ParadaRow(
    index: Int,
    parada: StopRequestDto,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrutalistBlack)
            .background(BrutalistWhite)
            .padding(12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Número de orden
        Box(
            modifier         = Modifier
                .size(32.dp)
                .border(2.dp, BrutalistBlack)
                .background(Color(0xFFF4F4F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = "$index",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize   = 14.sp,
                color      = BrutalistBlack
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = parada.destinatario.uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 13.sp,
                color      = BrutalistBlack,
                maxLines   = 1
            )
            Text(
                text       = parada.direccion,
                fontFamily = SpaceGroteskFamily,
                fontSize   = 11.sp,
                color      = Color(0xFF71717A),
                maxLines   = 1
            )
        }

        // Botón eliminar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clickable { onDelete() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Eliminar parada",
                tint               = SafetyOrange,
                modifier           = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
            }) {
                Text("OK", fontFamily = SpaceGroteskFamily, color = BrutalistBlack)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", fontFamily = SpaceGroteskFamily, color = SafetyOrange)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}