package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.slior.ui.components.SliorDesignTokens
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SpaceGroteskFamily

@Composable
fun RouteListScreen(
    repartidorId: String,
    onRouteClick: (String) -> Unit,
    onCreateRoute: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val state by viewModel.listState.collectAsStateWithLifecycle()

    LaunchedEffect(repartidorId) {
        viewModel.loadRoutes(repartidorId)
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
                .border(
                    width = 4.dp,
                    color = BrutalistBlack
                )
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "MIS RUTAS",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                letterSpacing = 2.sp,
                color = BrutalistBlack
            )
        }

        // Contenido
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (state) {
                is RouteListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                }
                is RouteListState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state as RouteListState.Error).message,
                            fontFamily = SpaceGroteskFamily,
                            color = BrutalistBlack,
                            modifier = Modifier
                                .background(BrutalistLightGray)
                                .border(2.dp, BrutalistBlack)
                                .padding(16.dp)
                        )
                    }
                }
                is RouteListState.Success -> {
                    val routes = (state as RouteListState.Success).routes
                    if (routes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "NO TIENES RUTAS ASIGNADAS",
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = BrutalistBlack,
                                letterSpacing = 1.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(routes) { route ->
                                RouteListItem(
                                    route = route,
                                    onClick = { onRouteClick(route.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Botón para crear ruta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            SliorPrimaryButton(
                text = "NUEVA RUTA",
                onClick = onCreateRoute,
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
        }
    }
}

@Composable
private fun RouteListItem(
    route: com.slior.data.remote.dto.RouteResponse,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrutalistWhite)
            .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
            .hardShadow(
                offsetX = SliorDesignTokens.ShadowOffset,
                offsetY = SliorDesignTokens.ShadowOffset
            )
            .clickable(enabled = true) { onClick() }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = route.nombre.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Route,
                    contentDescription = null,
                    tint = NeonGreen,
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "Fecha: ${route.fechaPlanificada}",
                fontFamily = SpaceGroteskFamily,
                fontSize = 12.sp,
                color = BrutalistBlack
            )

            Text(
                text = "Estado: ${route.status}",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NeonGreen
            )

            route.distanciaTotal?.let { distance ->
                Text(
                    text = "${String.format("%.2f", distance)} km · ${route.tiempoEstimado ?: "?"} min",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 11.sp,
                    color = BrutalistBlack
                )
            }
        }
    }
}