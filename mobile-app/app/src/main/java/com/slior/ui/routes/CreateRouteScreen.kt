package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.StopRequestDto
import com.slior.ui.components.SliorDesignTokens
import com.slior.ui.components.SliorFieldLabel
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.SliorTextField
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SafetyOrange
import com.slior.ui.theme.SpaceGroteskFamily

@Composable
fun CreateRouteScreen(
    repartidorId: String,
    onBack: () -> Unit,
    onRouteCreated: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val addressSuggestions by viewModel.addressSuggestions.collectAsStateWithLifecycle()
    val isLoadingAddresses by viewModel.isLoadingAddresses.collectAsStateWithLifecycle()

    var nombre by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    val paradas = remember { mutableStateListOf<StopRequestDto>() }

    // Campos para la parada que se está rellenando
    var stopDireccion by remember { mutableStateOf("") }
    var stopDestinatario by remember { mutableStateOf("") }
    var stopTelefono by remember { mutableStateOf("") }
    var stopLat by remember { mutableStateOf("") }
    var stopLon by remember { mutableStateOf("") }
    var showAddressSuggestions by remember { mutableStateOf(false) }

    // Navegar al éxito
    LaunchedEffect(createState) {
        if (createState is CreateRouteState.Success) {
            viewModel.resetCreateState()
            onRouteCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
        // TopAppBar brutalista
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistWhite)
                .border(4.dp, BrutalistBlack)
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = BrutalistBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "NUEVA RUTA",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Contenido scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sección: Datos de la ruta
            Text(
                text = "DATOS DE LA RUTA".uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.5.sp,
                color = BrutalistBlack
            )

            // Campo: Nombre
            SliorFieldLabel("Nombre de la ruta")
            SliorTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "Ej: Ruta Centro-Este",
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

            // Campo: Fecha
            SliorFieldLabel("Fecha (YYYY-MM-DD)")
            SliorTextField(
                value = fecha,
                onValueChange = { fecha = it },
                placeholder = "2026-03-20",
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

            // Campo: Notas
            SliorFieldLabel("Notas (opcional)")
            SliorTextField(
                value = notas,
                onValueChange = { notas = it },
                placeholder = "Información adicional...",
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

            // Divisor brutalista
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(NeonGreen)
            )

            // Sección: Paradas
            Text(
                text = "AÑADIR PARADAS".uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 1.5.sp,
                color = BrutalistBlack
            )

            // Campo: Dirección con autocompleta
            SliorFieldLabel("Dirección de entrega")
            Box(modifier = Modifier.fillMaxWidth()) {
                SliorTextField(
                    value = stopDireccion,
                    onValueChange = {
                        stopDireccion = it
                        viewModel.onAddressQueryChange(it)
                        showAddressSuggestions = it.isNotBlank()
                    },
                    placeholder = "Calle Principal, 123",
                    modifier = Modifier
                        .fillMaxWidth()
                        .hardShadow()
                )

                if (showAddressSuggestions && (addressSuggestions.isNotEmpty() || isLoadingAddresses)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .fillMaxWidth()
                            .offset(y = 70.dp)
                            .border(2.dp, BrutalistBlack)
                            .background(BrutalistWhite)
                            .zIndex(1000f)
                    ) {
                        if (isLoadingAddresses) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = BrutalistBlack
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(addressSuggestions) { suggestion ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                stopDireccion = suggestion.displayName
                                                stopLat = String.format("%.6f", suggestion.latitude)
                                                stopLon = String.format("%.6f", suggestion.longitude)
                                                showAddressSuggestions = false
                                                viewModel.selectAddress(suggestion)
                                            }
                                            .padding(12.dp)
                                            .border(
                                                bottom = 1.dp,
                                                color = BrutalistLightGray
                                            )
                                    ) {
                                        Text(
                                            text = suggestion.displayName,
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = BrutalistBlack
                                        )
                                        Text(
                                            text = "${String.format("%.4f", suggestion.latitude)}, ${String.format("%.4f", suggestion.longitude)}",
                                            fontFamily = SpaceGroteskFamily,
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 11.sp,
                                            color = BrutalistLightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Campo: Destinatario
            SliorFieldLabel("Destinatario")
            SliorTextField(
                value = stopDestinatario,
                onValueChange = { stopDestinatario = it },
                placeholder = "Nombre completo",
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

            // Campo: Teléfono
            SliorFieldLabel("Teléfono")
            SliorTextField(
                value = stopTelefono,
                onValueChange = { stopTelefono = it },
                placeholder = "+34 6XX XXX XXX",
                keyboardType = KeyboardType.Phone,
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

            // Campos: Latitud y Longitud (solo lectura)
            Text(
                text = "COORDENADAS (automáticas)".uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = BrutalistLightGray
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SliorFieldLabel("Latitud")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrutalistBlack)
                            .background(BrutalistLightGray)
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (stopLat.isBlank()) "--" else stopLat,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BrutalistBlack
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    SliorFieldLabel("Longitud")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, BrutalistBlack)
                            .background(BrutalistLightGray)
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = if (stopLon.isBlank()) "--" else stopLon,
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = BrutalistBlack
                        )
                    }
                }
            }

            // Botón: Añadir parada
            SliorPrimaryButton(
                text = "AÑADIR PARADA (${paradas.size})",
                onClick = {
                    val lat = stopLat.toDoubleOrNull()
                    val lon = stopLon.toDoubleOrNull()
                    if (stopDireccion.isNotBlank() && stopDestinatario.isNotBlank()
                        && stopTelefono.isNotBlank() && lat != null && lon != null
                    ) {
                        paradas.add(
                            StopRequestDto(stopDireccion, stopDestinatario, stopTelefono, lat, lon)
                        )
                        stopDireccion = ""
                        stopDestinatario = ""
                        stopTelefono = ""
                        stopLat = ""
                        stopLon = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = BrutalistBlack,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )

            // Lista de paradas añadidas
            if (paradas.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(SafetyOrange)
                )

                Text(
                    text = "PARADAS AÑADIDAS (${paradas.size})".uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 1.5.sp,
                    color = BrutalistBlack
                )

                paradas.forEachIndexed { index, parada ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            // MODIFICADOR CORREGIDO: sombra -> fondo -> borde
                            .hardShadow()
                            .background(BrutalistLightGray)
                            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${index + 1}. ${parada.destinatario.uppercase()}",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrutalistBlack
                            )
                            Text(
                                text = parada.direccion,
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 11.sp,
                                color = BrutalistBlack
                            )
                            Text(
                                text = "${parada.latitud}, ${parada.longitud}",
                                fontFamily = SpaceGroteskFamily,
                                fontSize = 10.sp,
                                color = SafetyOrange
                            )
                        }
                    }
                }
            }

            // Error banner
            if (createState is CreateRouteState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SafetyOrange)
                        .border(2.dp, BrutalistBlack)
                        .padding(12.dp)
                ) {
                    Text(
                        text = (createState as CreateRouteState.Error).message,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = BrutalistBlack
                    )
                }
            }

            // Botón: Guardar ruta
            val isEnabled =
                createState !is CreateRouteState.Loading && nombre.isNotBlank() && fecha.isNotBlank() && paradas.isNotEmpty()

            SliorPrimaryButton(
                text = if (createState is CreateRouteState.Loading) "GUARDANDO..." else "GUARDAR RUTA",
                onClick = {
                    viewModel.createRoute(
                        CreateRouteRequest(
                            nombre,
                            fecha,
                            repartidorId,
                            paradas.toList(),
                            notas.ifBlank { null }
                        )
                    )
                },
                enabled = isEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}