package com.slior.ui.routes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.remote.dto.CreateRouteRequest
import com.slior.data.remote.dto.StopRequestDto

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva ruta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Datos de la ruta
            Text("Datos de la ruta", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre de la ruta") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = fecha,
                onValueChange = { fecha = it },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = notas,
                onValueChange = { notas = it },
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider()

            // Añadir parada
            Text("Añadir parada", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = stopDireccion,
                onValueChange = { stopDireccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = stopDestinatario,
                onValueChange = { stopDestinatario = it },
                label = { Text("Destinatario") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = stopTelefono,
                onValueChange = { stopTelefono = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = stopLat,
                    onValueChange = { stopLat = it },
                    label = { Text("Latitud") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = stopLon,
                    onValueChange = { stopLon = it },
                    label = { Text("Longitud") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedButton(
                onClick = {
                    val lat = stopLat.toDoubleOrNull()
                    val lon = stopLon.toDoubleOrNull()
                    if (stopDireccion.isNotBlank() && stopDestinatario.isNotBlank()
                        && stopTelefono.isNotBlank() && lat != null && lon != null
                    ) {
                        paradas.add(StopRequestDto(stopDireccion, stopDestinatario, stopTelefono,
                            lat, lon))
                        stopDireccion = ""; stopDestinatario = ""; stopTelefono = ""
                        stopLat = ""; stopLon = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Añadir parada (${paradas.size} añadidas)")
            }

            // Error o botón guardar
            if (createState is CreateRouteState.Error) {
                Text(
                    text = (createState as CreateRouteState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.createRoute(
                        CreateRouteRequest(nombre, fecha, repartidorId, paradas.toList(),
                            notas.ifBlank { null })
                    )
                },
                enabled = createState !is CreateRouteState.Loading
                        && nombre.isNotBlank() && fecha.isNotBlank() && paradas.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (createState is CreateRouteState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth =
                        2.dp)
                } else {
                    Text("Guardar ruta")
                }
            }
        }
    }
}