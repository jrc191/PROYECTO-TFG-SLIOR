package com.slior.ui.routes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis rutas") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoute) {
                Icon(Icons.Default.Add, contentDescription = "Nueva ruta")
            }
        }
    ) { padding ->
        when (state) {
            is RouteListState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            is RouteListState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as RouteListState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is RouteListState.Success -> {
                val routes = (state as RouteListState.Success).routes
                if (routes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentAlignment = Alignment.Center
                    ) { Text("No tienes rutas asignadas") }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(routes) { route ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onRouteClick(route.id) },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = route.nombre,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Fecha: ${route.fechaPlanificada}")
                                    Text(
                                        text = "Estado: ${route.status}",
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    route.distanciaTotal?.let {
                                        Text("${"%.2f".format(it)} km · ${route.tiempoEstimado ?:
                                        "?"} min")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}