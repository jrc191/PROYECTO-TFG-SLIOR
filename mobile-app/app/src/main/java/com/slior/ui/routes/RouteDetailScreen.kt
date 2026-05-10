package com.slior.ui.routes

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.local.entity.StopEntity
import com.slior.ui.components.hardShadow
import com.slior.ui.map.RouteMapView
import com.slior.ui.map.toRouteMapPoint
import com.slior.ui.theme.*
import com.slior.util.NavigationHelper
import com.slior.util.Result
import kotlinx.coroutines.launch
import com.slior.viewmodel.AuthViewModel
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun RouteDetailScreen(
    routeId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val deleteState by viewModel.deleteState.collectAsStateWithLifecycle()
    
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
                snackbarHostState.showSnackbar(context.getString(R.string.map_info_address)) // Adjust if needed
            }
        }
    }

    LaunchedEffect(deleteState) {
        if (deleteState is Result.Success) {
            viewModel.resetDeleteState()
            onBack()
        } else if (deleteState is Result.Error) {
            snackbarHostState.showSnackbar(context.getString(R.string.state_error_loading))
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
            containerColor = BrutalistWhite,
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
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrutalistBlack
                )
            },
            text = { Text(stringResource(R.string.dialog_call_desc), fontFamily = SpaceGroteskFamily, color = BrutalistBlack) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToCall"))
                        context.startActivity(intent)
                        phoneToCall = null
                    },
                    color = NeonGreen, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_call), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = BrutalistBlack, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { phoneToCall = null }) {
                    Text(stringResource(R.string.btn_cancel), color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = BrutalistWhite,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = buildAnnotatedString {
                        append("¿")
                        withStyle(style = SpanStyle(color = SafetyOrange)) { append(stringResource(R.string.btn_delete)) }
                        append(" " + stringResource(R.string.title_routes).split(" ")[1] + "?") // Hacky for now
                    },
                    fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrutalistBlack
                )
            },
            text = { Text(stringResource(R.string.dialog_delete_route_desc), fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = BrutalistBlack) },
            confirmButton = {
                Surface(
                    modifier = Modifier.border(2.dp, BrutalistBlack).clickable { 
                        viewModel.deleteRoute(routeId)
                        showDeleteConfirm = false 
                    },
                    color = SafetyOrange, shape = MaterialTheme.shapes.extraSmall
                ) {
                    Text(text = stringResource(R.string.btn_delete), modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = BrutalistWhite, fontWeight = FontWeight.Bold, fontFamily = SpaceGroteskFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.btn_cancel), color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BrutalistWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp).zIndex(2f),
                color = BrutalistWhite, shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = BrutalistBlack, modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = when (state) {
                            is RouteDetailState.Success -> (state as RouteDetailState.Success).route.nombre.uppercase()
                            else -> stringResource(R.string.title_route_detail)
                        },
                        modifier = Modifier.weight(1f),
                        fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, color = BrutalistBlack
                    )
                    if (state is RouteDetailState.Success) {
                        IconButton(onClick = { onEdit(routeId) }) { Icon(Icons.Default.Edit, null, tint = BrutalistBlack) }
                        IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, null, tint = SafetyOrange) }
                    }
                }
            }

            if (state is RouteDetailState.Success) {
                val data = state as RouteDetailState.Success
                Box(modifier = Modifier.fillMaxWidth().height(280.dp).zIndex(1f).clipToBounds().drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }) {
                    RouteMapView(stops = data.stops.map { it.toRouteMapPoint() }, userLocation = currentLocation, centerKey = centerTrigger, onCallRequest = { phoneToCall = it }, modifier = Modifier.fillMaxSize())
                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                        Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = BrutalistWhite, shape = RectangleShape) {
                            Icon(Icons.Default.MyLocation, null, tint = BrutalistBlack, modifier = Modifier.padding(8.dp))
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
                            onNavigate = { NavigationHelper.launchExternalNavigation(context, data.stops.sortedBy { it.ordenVisita }) }
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 80.dp, end = 16.dp).zIndex(3f)) {
            Surface(modifier = Modifier.size(48.dp).hardShadow(2.dp, 2.dp).border(2.dp, BrutalistBlack).clickable { viewModel.loadRouteDetail(routeId); authViewModel.checkServerConnectivity(); if (hasLocationPermission(context)) viewModel.fetchCurrentLocation() }, color = BrutalistWhite, shape = MaterialTheme.shapes.extraSmall) {
                Box(contentAlignment = Alignment.Center) {
                    if (isRefreshing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrutalistBlack, strokeWidth = 2.dp)
                    else Icon(Icons.Default.Refresh, stringResource(R.string.status_verifying), tint = BrutalistBlack)
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun RouteDetailContent(data: RouteDetailState.Success, context: Context, currentLocation: Pair<Double, Double>?, onOptimize: () -> Unit, onNavigate: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Cabecera Fija: Tiempo Estimado
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF4F4F5))
                .drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(R.string.label_estimated_time), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp, color = BrutalistBlack)
            Spacer(Modifier.height(6.dp))
            Box(modifier = Modifier.hardShadow(2.dp, 2.dp).border(2.dp, BrutalistBlack).background(BrutalistWhite).padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(text = if (data.route.tiempoEstimado != null) formatMinutes(data.route.tiempoEstimado) else "--:--", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = SafetyOrange)
            }
            data.route.distanciaTotal?.let {
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.format_km, it) + " · " + stringResource(R.string.label_stops_count, data.stops.size), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = BrutalistBlack)
            }
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hardShadow()
                        .border(2.dp, BrutalistBlack)
                        .background(Color(0xFFF4F4F5))
                        .clickable { onOptimize() }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(stringResource(R.string.btn_optimize), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp, color = BrutalistBlack)
                }
            }
        }

        // 2. Botón Optimizar Fijo


        // 3. Lista Deslizable de Paradas
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(data.stops.sortedBy { it.ordenVisita }) { stop ->
                BrutalistStopRow(stop = stop)
            }
        }

        // 4. Botón Iniciar Navegación Fijo al final
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistWhite)
                .drawBehind { drawLine(BrutalistBlack, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx()) }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .hardShadow(2.dp, 2.dp)
                    .border(2.dp, BrutalistBlack)
                    .background(NeonGreen)
                    .clickable { onNavigate() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.Navigation, null, tint = BrutalistBlack, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(stringResource(R.string.btn_start_navigation), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp, color = BrutalistBlack)
            }
        }
    }
}

@Composable
private fun BrutalistStopRow(stop: StopEntity) {
    Row(modifier = Modifier.fillMaxWidth().drawBehind { drawLine(Color(0xFFE4E4E7), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(32.dp).border(2.dp, BrutalistBlack).background(if (stop.status == "ENTREGADO") NeonGreen else Color(0xFFF4F4F5)), contentAlignment = Alignment.Center) { Text("${stop.ordenVisita}", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, color = BrutalistBlack) }
        Column(modifier = Modifier.weight(1f)) {
            Text(stop.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (stop.status == "ENTREGADO") Color.Gray else BrutalistBlack)
            Text(stop.direccion, fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = if (stop.status == "ENTREGADO") Color.Gray else BrutalistBlack, maxLines = 1)
        }
        if (stop.status == "ENTREGADO") { Text("✓", fontWeight = FontWeight.Black, color = NeonGreen, fontSize = 20.sp) }
    }
}

@Composable
private fun RouteDetailLoading() { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = BrutalistBlack) } }

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
