package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.slior.ui.map.RouteMapView
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SpaceGroteskFamily

@Composable
fun NavigationScreen(
    routeId: String,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val state by viewModel.navigationState.collectAsStateWithLifecycle()

    LaunchedEffect(routeId) {
        viewModel.loadRouteForNavigation(routeId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
        // TopAppBar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistWhite)
                .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
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
                    text = "NAVEGACIÓN",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    letterSpacing = 1.5.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Contenido
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
            when (state) {
                is NavigationState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonGreen)
                    }
                }

                is NavigationState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (state as NavigationState.Error).message,
                            fontFamily = SpaceGroteskFamily,
                            color = BrutalistBlack
                        )
                    }
                }

                is NavigationState.Success -> {
                    val data = state as NavigationState.Success
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Mapa
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(
                                    SliorDesignTokens.BorderWidthHeavy,
                                    BrutalistBlack
                                )
                        ) {
                            RouteMapView(
                                stops = data.stops,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Info de paradas activas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    SliorDesignTokens.BorderWidthHeavy,
                                    BrutalistBlack
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "PARADAS PENDIENTES: ${data.stops.count { it.status.uppercase() != "COMPLETADA" }}",
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.sp,
                                    color = BrutalistBlack
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
