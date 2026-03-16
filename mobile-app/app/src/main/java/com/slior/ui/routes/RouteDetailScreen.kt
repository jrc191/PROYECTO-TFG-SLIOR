package com.slior.ui.routes

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.ui.map.RouteMapView

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de ruta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        when (state) {
            is RouteDetailState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is RouteDetailState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as RouteDetailState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is RouteDetailState.Success -> {
                val data = state as RouteDetailState.Success
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Mapa
                    item {
                        RouteMapView(
                            stops = data.stops,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }

                    // Info de la ruta
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = data.route.nombre,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Estado: ${data.route.status}")
                                Text("Fecha: ${data.route.fechaPlanificada}")
                                data.route.distanciaTotal?.let {
                                    Text("Distancia: ${"%.2f".format(it)} km")
                                }
                                data.route.tiempoEstimado?.let {
                                    Text("Tiempo estimado: $it min")
                                }
                            }
                        }
                    }

                    // Botón optimizar
                    item {
                        Button(
                            onClick = { viewModel.optimizeRoute(routeId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Optimizar ruta")
                        }
                    }

                    // Lista de paradas
                    item {
                        Text(
                            text = "Paradas (${data.stops.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(data.stops) { stop ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Badge { Text("${stop.ordenVisita}") }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = stop.destinatario,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = stop.direccion,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Estado: ${stop.status}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}