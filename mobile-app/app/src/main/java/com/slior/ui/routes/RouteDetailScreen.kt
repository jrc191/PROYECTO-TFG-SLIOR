package com.slior.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.local.entity.StopEntity
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.optimizeRoute(routeId)
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Concede permiso de ubicación para optimizar")
            }
        }
    }

    LaunchedEffect(routeId) {
        viewModel.loadRouteDetail(routeId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── TopAppBar ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BrutalistWhite)
                    .drawBehind {
                        drawLine(
                            color = BrutalistBlack,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
                    .padding(horizontal = 8.dp)
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = BrutalistBlack,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (state) {
                        is RouteDetailState.Success ->
                            (state as RouteDetailState.Success).route.nombre.uppercase()
                        else -> "DETALLE DE RUTA"
                    },
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    letterSpacing = (-0.5).sp,
                    color = BrutalistBlack
                )
            }

            // ── Contenido ──────────────────────────────────────────────
            when (state) {
                is RouteDetailState.Loading -> RouteDetailLoading()
                is RouteDetailState.Error   -> RouteDetailError(
                    message = (state as RouteDetailState.Error).message
                )
                is RouteDetailState.Success -> {
                    val data = state as RouteDetailState.Success
                    RouteDetailContent(
                        data           = data,
                        context        = context,
                        onOptimize     = {
                            if (hasLocationPermission(context)) {
                                viewModel.optimizeRoute(routeId)
                            } else {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                    )
                }
            }
        }

        // ── Snackbar ───────────────────────────────────────────────────
        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RUTAS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RouteDetailContent(
    data: RouteDetailState.Success,
    context: Context,
    onOptimize: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            modifier            = Modifier.fillMaxSize(),
            contentPadding      = PaddingValues(bottom = 96.dp)
        ) {
            // Mapa
            item {
                RouteMapView(
                    stops    = data.stops,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }

            // tiempo estimado
            item {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF4F4F5))
                        .drawBehind {
                            drawLine(
                                color       = BrutalistBlack,
                                start       = Offset(0f, size.height),
                                end         = Offset(size.width, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text          = "TIEMPO ESTIMADO",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 13.sp,
                        letterSpacing = 2.sp,
                        color         = BrutalistBlack
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .hardShadow(offsetX = 2.dp, offsetY = 2.dp)
                            .border(2.dp, BrutalistBlack)
                            .background(BrutalistWhite)
                            .padding(horizontal = 24.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text       = if (data.route.tiempoEstimado != null)
                                formatMinutes(data.route.tiempoEstimado)
                            else "--:--",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 28.sp,
                            color      = SafetyOrange
                        )
                    }
                    // Distancia
                    data.route.distanciaTotal?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text       = "${"%.2f".format(it)} km · ${data.stops.size} paradas",
                            fontFamily = SpaceGroteskFamily,
                            fontSize   = 12.sp,
                            color      = Color(0xFF71717A)
                        )
                    }
                }
            }

            // Lista de paradas
            items(data.stops.sortedBy { it.ordenVisita }) { stop ->
                BrutalistStopRow(stop = stop)
            }

            // Botón optimizar
            item {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .hardShadow()
                            .border(2.dp, BrutalistBlack)
                            .background(Color(0xFFF4F4F5))
                            .clickable { onOptimize() }
                            .padding(16.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text          = "⚡ OPTIMIZAR ORDEN",
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 15.sp,
                            letterSpacing = 1.sp,
                            color         = BrutalistBlack
                        )
                    }
                }
            }
        }

        // ── Botón "INICIAR NAVEGACIÓN" ──────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(BrutalistWhite)
                .drawBehind {
                    drawLine(
                        color       = BrutalistBlack,
                        start       = Offset(0f, 0f),
                        end         = Offset(size.width, 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp)
                    .border(2.dp, BrutalistBlack)
                    .background(NeonGreen)
                    .clickable { /* navegación futura */ },
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector        = Icons.Default.Navigation,
                    contentDescription = null,
                    tint               = BrutalistBlack,
                    modifier           = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text          = "INICIAR NAVEGACIÓN",
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 18.sp,
                    letterSpacing = 1.sp,
                    color         = BrutalistBlack
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Fila de parada
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun BrutalistStopRow(stop: StopEntity) {
    val isCompleted = stop.status.uppercase() == "ENTREGADO"
    val isActive    = stop.status.uppercase() == "EN_CAMINO"

    val checkboxColor = when {
        isCompleted -> NeonGreen
        isActive    -> SafetyOrange
        else        -> BrutalistWhite
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .drawBehind {
                // Borde inferior
                drawLine(
                    color       = BrutalistBlack,
                    start       = Offset(0f, size.height),
                    end         = Offset(size.width, size.height),
                    strokeWidth = 1.dp.toPx()
                )
            }
            // Barra lateral naranja si está activa
            .then(
                if (isActive) Modifier.drawBehind {
                    drawRect(
                        color = SafetyOrange,
                        size  = androidx.compose.ui.geometry.Size(8.dp.toPx(), size.height)
                    )
                } else Modifier
            )
            .background(BrutalistWhite)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox de estado
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(2.dp, BrutalistBlack)
                .background(checkboxColor),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text(text = "✓", fontSize = 16.sp, fontWeight = FontWeight.Black, color = BrutalistBlack)
            }
        }

        Spacer(Modifier.width(14.dp))

        // Dirección y número de entrega
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text           = stop.direccion,
                fontFamily     = SpaceGroteskFamily,
                fontWeight     = FontWeight.Bold,
                fontSize       = 16.sp,
                color          = if (isCompleted) Color(0xFF9E9E9E) else BrutalistBlack,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                maxLines       = 1
            )
            Text(
                text       = "ENTREGA #${stop.id.takeLast(4).uppercase()}",
                fontFamily = SpaceGroteskFamily,
                fontSize   = 12.sp,
                color      = Color(0xFF71717A)
            )
        }

        // Badge "AHORA" si está activa
        if (isActive) {
            Box(
                modifier = Modifier
                    .border(2.dp, BrutalistBlack)
                    .background(SafetyOrange)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text       = "AHORA",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize   = 11.sp,
                    color      = BrutalistBlack
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Estados: Loading / Error
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RouteDetailLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = BrutalistBlack, strokeWidth = 3.dp)
    }
}

@Composable
private fun RouteDetailError(message: String) {
    Box(
        modifier         = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "ERROR",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize   = 22.sp,
                color      = SafetyOrange
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = message,
                fontFamily = SpaceGroteskFamily,
                fontSize   = 14.sp,
                color      = Color(0xFF71717A)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "%02d:%02d h".format(h, m) else "%02d min".format(m)
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}