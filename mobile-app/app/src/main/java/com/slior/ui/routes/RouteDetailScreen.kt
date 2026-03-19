package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.remote.dto.StopResponseDto
import com.slior.ui.components.SliorDesignTokens
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SafetyOrange
import com.slior.ui.theme.SpaceGroteskFamily

@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.loadRouteDetail(routeId)
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
                    text = "DETALLE RUTA",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 2.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Contenido
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (state) {
                is RouteDetailState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                }

                is RouteDetailState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state as RouteDetailState.Error).message,
                            fontFamily = SpaceGroteskFamily,
                            color = BrutalistBlack,
                            modifier = Modifier
                                .background(BrutalistLightGray)
                                .border(2.dp, BrutalistBlack)
                                .padding(16.dp)
                        )
                    }
                }

                is RouteDetailState.Success -> {
                    val data = state as RouteDetailState.Success
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Mapa
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .border(
                                        SliorDesignTokens.BorderWidthHeavy,
                                        BrutalistBlack
                                    )
                                    .hardShadow()
                            ) {
                                RouteMapView(
                                    stops = data.stops,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // Info de la ruta
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BrutalistWhite)
                                    .border(
                                        SliorDesignTokens.BorderWidthHeavy,
                                        BrutalistBlack
                                    )
                                    .hardShadow()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = data.route.nombre.uppercase(),
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 16.sp,
                                        letterSpacing = 1.sp,
                                        color = BrutalistBlack
                                    )

                                    Text(
                                        text = "Estado: ${data.route.status}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NeonGreen
                                    )

                                    Text(
                                        text = "Fecha: ${data.route.fechaPlanificada}",
                                        fontFamily = SpaceGroteskFamily,
                                        fontSize = 12.sp,
                                        color = BrutalistBlack
                                    )

                                    data.route.distanciaTotal?.let { distance ->
                                        Text(
                                            text = "Distancia: ${String.format("%.2f", distance)} km",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 12.sp,
                                            color = BrutalistBlack
                                        )
                                    }

                                    data.route.tiempoEstimado?.let { time ->
                                        Text(
                                            text = "Tiempo estimado: $time min",
                                            fontFamily = SpaceGroteskFamily,
                                            fontSize = 12.sp,
                                            color = BrutalistBlack
                                        )
                                    }
                                }
                            }
                        }

                        // Botón optimizar
                        item {
                            SliorPrimaryButton(
                                text = "OPTIMIZAR RUTA",
                                onClick = { viewModel.optimizeRoute(routeId) },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Route,
                                        contentDescription = null,
                                        tint = BrutalistBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }

                        // Título paradas
                        item {
                            Text(
                                text = "PARADAS (${data.stops.size})".uppercase(),
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                letterSpacing = 1.sp,
                                color = BrutalistBlack
                            )
                        }

                        // Lista de paradas
                        items(data.stops) { stop ->
                            StopCard(stop = stop)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StopCard(stop: StopResponseDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrutalistWhite)
            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
            .hardShadow(
                offsetX = SliorDesignTokens.ShadowOffset,
                offsetY = SliorDesignTokens.ShadowOffset
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badge con número de orden
            Box(
                modifier = Modifier
                    .background(NeonGreen)
                    .border(2.dp, BrutalistBlack)
                    .size(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stop.ordenVisita.toString(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = BrutalistBlack
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stop.destinatario.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp,
                    color = BrutalistBlack
                )

                Text(
                    text = stop.direccion,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = BrutalistBlack
                )

                Text(
                    text = "Estado: ${stop.status}",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = NeonGreen
                )
            }
        }
    }
}