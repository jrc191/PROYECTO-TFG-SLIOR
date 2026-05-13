package com.slior.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.local.entity.StopEntity
import com.slior.data.local.entity.SyncStatus
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.map.toRouteMapPoint
import com.slior.ui.theme.*
import com.slior.util.NavigationHelper
import com.slior.util.Result
import com.slior.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * Pantalla de Detalle de Ruta.
 * Muestra el mapa, estadísticas de tiempo y la lista de paradas.
 * Soporta Modo Claro/Oscuro y Landscape.
 */
@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onStopClick: (String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
    val isRetryingSync by viewModel.isRetryingSync.collectAsStateWithLifecycle()
    
    // Colores dinámicos
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    val isRefreshing = state is RouteDetailState.Loading
    var centerTrigger by remember { mutableStateOf(0) }
    var phoneToCall by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.optimizeRoute(routeId)
            viewModel.fetchCurrentLocation()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Permisos de ubicación necesarios")
            }
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is Result.Success) {
            viewModel.resetDeleteState()
            onBack()
        } else if (deleteState is Result.Error) {
            snackbarHostState.showSnackbar("Error al eliminar ruta")
            viewModel.resetDeleteState()
        }
    }

    LaunchedEffect(routeId) {
        viewModel.loadRouteDetail(routeId)
        if (hasLocationPermission(context)) {
            viewModel.startLocationTracking()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    if (phoneToCall != null) {
        AlertDialog(
            onDismissRequest = { phoneToCall = null },
            containerColor = surfaceColor,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.dialog_call_title).split("%")[0])
                        withStyle(style = SpanStyle(color = SafetyOrange)) { append(phoneToCall!!) }
                        append("?")
                    },
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurfaceColor
                )
            },
            text = { Text(stringResource(R.string.dialog_call_desc), fontFamily = SpaceGroteskFamily, color = onSurfaceColor) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                        context.startActivity(intent)
                        phoneToCall = null
                    },
                    color = NeonGreen, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_call), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneToCall = null }) {
                    Text(stringResource(R.string.btn_cancel), color = onSurfaceColor, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = surfaceColor,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = buildAnnotatedString {
                        append("¿")
                        withStyle(style = SpanStyle(color = SafetyOrange)) { append(stringResource(R.string.btn_delete)) }
                        append(" " + stringResource(R.string.title_routes).split(" ")[1] + "?")
                    },
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurfaceColor
                )
            },
            text = { Text(stringResource(R.string.dialog_delete_route_desc), fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = onSurfaceColor) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        viewModel.deleteRoute(routeId)
                        showDeleteConfirm = false 
                    },
                    color = SafetyOrange, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_delete), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = Color.White, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel), color = onSurfaceColor, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(if (isLandscape) 56.dp else 64.dp).zIndex(2f),
                color = surfaceColor, shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().drawBehind { drawLine(onSurfaceColor, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = onSurfaceColor, modifier = Modifier.size(if (isLandscape) 24.dp else 28.dp))
                    }
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (state) {
                                is RouteDetailState.Success -> (state as RouteDetailState.Success).route.nombre.uppercase()
                                else -> stringResource(R.string.title_route_detail)
                            },
                            fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 14.sp else 18.sp, maxLines = 1, color = onSurfaceColor
                        )
                        if (isLandscape && state is RouteDetailState.Success) {
                            val data = state as RouteDetailState.Success
                            val r = data.route
                            val total = data.stops.size
                            val pending = data.stops.count { it.status != "ENTREGADO" }
                            Text(
                                text = "${if (r.tiempoEstimado != null) formatMinutes(r.tiempoEstimado) else "--"} · ${if (r.distanciaTotal != null) stringResource(R.string.format_km, r.distanciaTotal) else "--"} · $pending/$total " + stringResource(R.string.title_routes).split(" ")[1].lowercase(),
                                fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = SafetyOrange, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (state is RouteDetailState.Success) {
                        if (isLandscape) {
                            IconButton(onClick = { 
                                viewModel.loadRouteDetail(routeId)
                                authViewModel.checkServerConnectivity()
                                if (hasLocationPermission(context)) viewModel.fetchCurrentLocation()
                            }) {
                                if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = onSurfaceColor, strokeWidth = 2.dp)
                                else Icon(Icons.Default.Refresh, null, tint = onSurfaceColor)
                            }
                            IconButton(onClick = { if (hasLocationPermission(context)) viewModel.optimizeRoute(routeId) else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }) {
                                Icon(Icons.Default.AutoFixHigh, null, tint = onSurfaceColor)
                            }
                        }
                        IconButton(onClick = { onEdit(routeId) }) { Icon(Icons.Default.Edit, null, tint = onSurfaceColor, modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)) }
                        IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, null, tint = SafetyOrange, modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp)) }
                    }
                }
            }

            if (isLandscape) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (state is RouteDetailState.Success) {
                        val data = state as RouteDetailState.Success
                        Box(modifier = Modifier.weight(1.2f).fillMaxHeight().border(2.dp, BrutalistBlack).clipToBounds()) {
                            RouteMapView(stops = data.stops.map { it.toRouteMapPoint() }, userLocation = currentLocation, centerKey = centerTrigger, onCallRequest = { phoneToCall = it }, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                                Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = surfaceColor, shape = RectangleShape) {
                                    Icon(Icons.Default.MyLocation, null, tint = onSurfaceColor, modifier = Modifier.padding(8.dp))
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        when (state) {
                            is RouteDetailState.Loading -> if (!isRefreshing) RouteDetailLoading()
                            is RouteDetailState.Error   -> RouteDetailError(message = (state as RouteDetailState.Error).message)
                            is RouteDetailState.Success -> {
                                val data = state as RouteDetailState.Success
                                RouteDetailContent(
                                    data = data, context = context, currentLocation = currentLocation,
                                    onOptimize = { viewModel.optimizeRoute(routeId) },
                                    onNavigate = { NavigationHelper.launchExternalNavigation(context, data.stops.sortedBy { it.ordenVisita }) },
                                    onNavigateToStop = { stop ->
                                        // Usamos geo: para mostrar el punto en el mapa sin iniciar ruta automáticamente
                                        val label = Uri.encode(stop.destinatario)
                                        val gmmIntentUri = Uri.parse("geo:0,0?q=${stop.latitud},${stop.longitud}($label)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    },
                                    onStopClick = onStopClick,
                                    isLandscape = true
                                )
                            }
                        }
                    }
                }
            } else {
                if (state is RouteDetailState.Success) {
                    val data = state as RouteDetailState.Success
                    Box(modifier = Modifier.fillMaxWidth().height(260.dp).zIndex(1f).clipToBounds().drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) } ) {
                        RouteMapView(stops = data.stops.map { it.toRouteMapPoint() }, userLocation = currentLocation, centerKey = centerTrigger, onCallRequest = { phoneToCall = it }, modifier = Modifier.fillMaxSize())
                        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = surfaceColor, shape = RectangleShape) {
                                Icon(Icons.Default.MyLocation, null, tint = onSurfaceColor, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).zIndex(0f)) {
                    when (state) {
                        is RouteDetailState.Loading -> if (!isRefreshing) RouteDetailLoading()
                        is RouteDetailState.Error   -> RouteDetailError(message = (state as RouteDetailState.Error).message)
                        is RouteDetailState.Success -> {
                            val data = state as RouteDetailState.Success
                            RouteDetailContent(
                                data = data, context = context, currentLocation = currentLocation,
                                onOptimize = { if (hasLocationPermission(context)) viewModel.optimizeRoute(routeId) else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) },
                                    onNavigate = { NavigationHelper.launchExternalNavigation(context, data.stops.sortedBy { it.ordenVisita }) },
                                    onNavigateToStop = { stop ->
                                        // Usamos geo: para mostrar el punto en el mapa sin iniciar ruta automáticamente
                                        val label = Uri.encode(stop.destinatario)
                                        val gmmIntentUri = Uri.parse("geo:0,0?q=${stop.latitud},${stop.longitud}($label)")
                                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        context.startActivity(mapIntent)
                                    },
                                    onStopClick = onStopClick,
                                    isLandscape = false
                            )
                        }
                    }
                }
            }
        }

        // Banner de Sin Conexión - Superior
        if (!isOnline) {
            ConnectionBanner(
                onRetry = { viewModel.retrySyncStops() },
                isRetryingSync = isRetryingSync,
                modifier = Modifier
                    .padding(top = if (isLandscape) 56.dp else 64.dp)
                    .zIndex(10f)
            )
        }

        if (!isLandscape) {
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp).zIndex(3f)) {
                Surface(modifier = Modifier.size(48.dp).hardShadow(2.dp, 2.dp).border(2.dp, BrutalistBlack).clickable { viewModel.loadRouteDetail(routeId); authViewModel.checkServerConnectivity(); if (hasLocationPermission(context)) viewModel.fetchCurrentLocation() }, color = surfaceColor, shape = MaterialTheme.shapes.extraSmall) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = onSurfaceColor, strokeWidth = 2.dp)
                        else Icon(Icons.Default.Refresh, stringResource(R.string.status_verifying), tint = onSurfaceColor)
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))

        if (!isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = if (isLandscape) 12.dp else 90.dp)
                    .zIndex(10f)
            ) {
                ConnectionBanner(
                    onRetry = { viewModel.retrySyncStops() },
                    isRetryingSync = isRetryingSync
                )
            }
        }
    }
}

@Composable
private fun ConnectionBanner(onRetry: () -> Unit, isRetryingSync: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .hardShadow(4.dp, 4.dp)
            .border(2.dp, BrutalistBlack)
            .clickable { onRetry() },
        color = SafetyOrange,
        shape = RectangleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.CloudOff, null, tint = Color.White)
            Text(
                text = stringResource(R.string.banner_offline_mode),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            if (isRetryingSync) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Surface(
                    color = Color.White,
                    shape = RectangleShape,
                    modifier = Modifier.border(1.dp, BrutalistBlack)
                ) {
                    Text(
                        text = stringResource(R.string.btn_retry),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun RouteDetailContent(
    data: RouteDetailState.Success, 
    context: Context, 
    currentLocation: Pair<Double, Double>?, 
    onOptimize: () -> Unit, 
    onNavigate: () -> Unit,
    onNavigateToStop: (StopEntity) -> Unit,
    onStopClick: (String) -> Unit,
    isLandscape: Boolean
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isLandscape) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(onSurface.copy(alpha = 0.05f))
                    .drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(R.string.label_estimated_time), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp, color = onSurface)
                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.hardShadow(2.dp, 2.dp).border(2.dp, BrutalistBlack).background(surface).padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(text = if (data.route.tiempoEstimado != null) formatMinutes(data.route.tiempoEstimado) else "--:--", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = SafetyOrange)
                }
                data.route.distanciaTotal?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(stringResource(R.string.format_km, it) + " · " + stringResource(R.string.label_stops_count, data.stops.size), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = onSurface)
                }
            }

            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hardShadow()
                        .border(2.dp, BrutalistBlack)
                        .background(onSurface.copy(alpha = 0.05f))
                        .clickable { onOptimize() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.btn_optimize), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp, color = onSurface)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(data.stops.sortedBy { it.ordenVisita }) { stop ->
                BrutalistStopRow(
                    stop = stop, 
                    onClick = { onStopClick(stop.id) },
                    onNavigateClick = { onNavigateToStop(stop) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(surface)
                .drawBehind { drawLine(BrutalistBlack, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx()) }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLandscape) 48.dp else 64.dp)
                    .hardShadow(2.dp, 2.dp)
                    .border(2.dp, BrutalistBlack)
                    .background(NeonGreen)
                    .clickable { onNavigate() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Navigation, null, tint = Color.Black, modifier = Modifier.size(if (isLandscape) 20.dp else 24.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.btn_start_navigation), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 14.sp else 18.sp, letterSpacing = 1.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun BrutalistStopRow(
    stop: StopEntity, 
    onClick: () -> Unit,
    onNavigateClick: () -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .drawBehind { drawLine(onSurface.copy(alpha = 0.1f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier.size(32.dp).border(2.dp, BrutalistBlack).background(if (stop.status == "ENTREGADO") NeonGreen else onSurface.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) { 
            Text("${stop.ordenVisita}", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (stop.status == "ENTREGADO") Color.Black else onSurface) 
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(stop.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (stop.status == "ENTREGADO") onSurface.copy(alpha = 0.5f) else onSurface)
            Text(stop.direccion, fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = if (stop.status == "ENTREGADO") onSurface.copy(alpha = 0.5f) else onSurface, maxLines = 1)
        }
        
        if (stop.status != "ENTREGADO") {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, BrutalistBlack)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { onNavigateClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe, 
                    contentDescription = null,
                    tint = BrutalistBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (stop.status == "ENTREGADO") {
            when (stop.syncStatus) {
                SyncStatus.PENDING.name -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "sync")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
                        label = "rotation"
                    )
                    Icon(Icons.Default.Sync, stringResource(R.string.label_sync_pending), tint = onSurface.copy(alpha = 0.5f), modifier = Modifier.size(18.dp).graphicsLayer(rotationZ = rotation))
                }
                SyncStatus.FAILED.name -> {
                    Icon(Icons.Default.CloudOff, stringResource(R.string.label_sync_error), tint = SafetyOrange, modifier = Modifier.size(18.dp))
                }
                else -> {
                    Text("✓", fontWeight = FontWeight.Black, color = NeonGreen, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
private fun RouteDetailLoading() { 
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = onSurface) } 
}

@Composable
private fun RouteDetailError(message: String) { Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) { Text(message, color = SafetyOrange, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold) } }

private fun formatMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return if (h > 0) "${h}h ${m}m" else "${m}min"
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
           ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

private fun Modifier.border(bottom: androidx.compose.ui.unit.Dp, color: Color) = this.drawBehind {
    val strokeWidth = bottom.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
}
