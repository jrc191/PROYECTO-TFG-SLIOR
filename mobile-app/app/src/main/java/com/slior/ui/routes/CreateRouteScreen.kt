package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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

            // Campo: Dirección
            SliorFieldLabel("Dirección de entrega")
            SliorTextField(
                value = stopDireccion,
                onValueChange = { stopDireccion = it },
                placeholder = "Calle Principal, 123",
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow()
            )

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

            // Campos: Latitud y Longitud
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SliorFieldLabel("Latitud")
                    SliorTextField(
                        value = stopLat,
                        onValueChange = { stopLat = it },
                        placeholder = "40.4168",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow()
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    SliorFieldLabel("Longitud")
                    SliorTextField(
                        value = stopLon,
                        onValueChange = { stopLon = it },
                        placeholder = "-3.7038",
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow()
                    )
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
                            .background(BrutalistLightGray)
                            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
                            .hardShadow()
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