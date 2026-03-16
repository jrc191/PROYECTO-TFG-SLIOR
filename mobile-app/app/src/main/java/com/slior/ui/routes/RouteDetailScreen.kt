package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.local.entity.StopEntity
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(BrutalistWhite)
                    ) {
                        // Sección de Tiempo Estimado
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BrutalistLightGray)
                                .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TIEMPO ESTIMADO",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.sp,
                                    color = BrutalistBlack
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Box(
                                    modifier = Modifier
                                        .background(BrutalistWhite)
                                        .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
                                        .hardShadow(
                                            offsetX = SliorDesignTokens.ShadowOffsetSmall,
                                            offsetY = SliorDesignTokens.ShadowOffsetSmall
                                        )
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (data.route.tiempoEstimado != null) {
                                            val hours = data.route.tiempoEstimado / 60
                                            val minutes = data.route.tiempoEstimado % 60
                                            String.format("%02d:%02d:%02d", hours, minutes, 0)
                                        } else {
                                            "??:??:??"
                                        },
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp,
                                        color = SafetyOrange
                                    )
                                }
                            }
                        }

                        // LazyColumn con contenido
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f)
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

        // Botón flotante INICIAR NAVEGACIÓN
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomEnd)
                .background(BrutalistWhite)
                .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = { /* TODO: Iniciar navegación */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonGreen,
                    contentColor = BrutalistBlack
                ),
                shape = androidx.compose.foundation.shape.RectangleShape
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = BrutalistBlack,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(end = 8.dp)
                    )
                    Text(
                        text = "INICIAR NAVEGACIÓN",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StopCard(stop: com.slior.data.local.entity.StopEntity) {
    val isCompleted = stop.status.uppercase() == "COMPLETADA"
    val isActive = stop.status.uppercase() == "EN_CURSO"
    val statusColor = if (isCompleted) NeonGreen else if (isActive) SafetyOrange else BrutalistWhite
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .background(BrutalistWhite)
            .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
            .hardShadow(
                offsetX = SliorDesignTokens.ShadowOffset,
                offsetY = SliorDesignTokens.ShadowOffset
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicador de estado (checkbox o check)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(statusColor)
                    .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = BrutalistBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Contenido de la parada
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stop.destinatario.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrutalistBlack,
                    textDecoration = if (isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None,
                    modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                )

                Text(
                    text = "ENTREGA #${stop.id.takeLast(4)}",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                )
            }

            // Badge "AHORA" si está activa
            if (isActive) {
                Box(
                    modifier = Modifier
                        .background(SafetyOrange)
                        .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AHORA",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistBlack
                    )
                }
            }
        }
    }
}