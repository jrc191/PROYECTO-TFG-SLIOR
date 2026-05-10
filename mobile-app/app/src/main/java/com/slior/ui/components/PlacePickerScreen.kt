package com.slior.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.remote.dto.AddressSuggestion
import com.slior.ui.map.PlacePickerMap
import com.slior.ui.map.RouteMapPoint
import com.slior.ui.routes.RouteViewModel
import com.slior.ui.theme.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import com.slior.R

@Composable
fun PlacePickerScreen(
    onLocationConfirmed: (AddressSuggestion) -> Unit,
    onBack: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    lastStopLocation: Pair<Double, Double>? = null,
    existingStops: List<RouteMapPoint> = emptyList(),
    onCallRequest: (String) -> Unit = {}
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var hasCentered by rememberSaveable { mutableStateOf(false) }
    var currentZoom by rememberSaveable { mutableDoubleStateOf(15.0) }
    var currentCenterLat by rememberSaveable { mutableDoubleStateOf(0.0) }
    var currentCenterLon by rememberSaveable { mutableDoubleStateOf(0.0) }
    var centerTrigger by remember { mutableStateOf(0) }
    
    val selectedLocation by viewModel.pickedLocation.collectAsStateWithLifecycle()
    val suggestions by viewModel.addressSuggestions.collectAsStateWithLifecycle()
    val currentLocation by viewModel.currentLocation.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolvingAddress.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.startLocationTracking()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLocationTracking()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BrutalistWhite)) {
        PlacePickerMap(
            selectedLocation = selectedLocation,
            defaultCenter = lastStopLocation ?: currentLocation,
            hasCenteredOnce = hasCentered,
            onCentered = { hasCentered = true },
            initialZoom = currentZoom,
            onZoomChanged = { currentZoom = it },
            initialCenter = if (currentCenterLat != 0.0) currentCenterLat to currentCenterLon else null,
            onCenterChanged = { lat, lon -> currentCenterLat = lat; currentCenterLon = lon },
            onLocationSelected = { lat, lon -> viewModel.selectLocationFromMap(lat, lon) },
            existingStops = existingStops,
            centerKey = centerTrigger,
            onCallRequest = onCallRequest,
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier.align(Alignment.BottomEnd).padding(bottom = 120.dp, end = 16.dp)) {
            Surface(modifier = Modifier.size(40.dp).border(2.dp, BrutalistBlack).clickable { centerTrigger++ }, color = BrutalistWhite, shape = RectangleShape) {
                Icon(imageVector = Icons.Default.MyLocation, contentDescription = stringResource(R.string.status_verifying), tint = BrutalistBlack, modifier = Modifier.padding(8.dp))
            }
        }

        Column(modifier = Modifier.statusBarsPadding().padding(16.dp).align(Alignment.TopCenter)) {
            Surface(modifier = Modifier.fillMaxWidth().hardShadow().border(2.dp, BrutalistBlack), color = BrutalistWhite) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = BrutalistBlack) }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it; viewModel.searchAddressSuggestions(it) },
                        modifier = Modifier.weight(1f).padding(vertical = 12.dp),
                        textStyle = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrutalistBlack),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) { Text(stringResource(R.string.placeholder_search_address), fontFamily = SpaceGroteskFamily, fontSize = 16.sp, color = Color(0xFFB0B0B0)) }
                                innerTextField()
                            }
                        }
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = ""; viewModel.clearAddressSuggestions() }) { Icon(Icons.Default.Close, null, tint = BrutalistBlack) }
                    }
                }
            }
            AnimatedVisibility(visible = suggestions.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
                Surface(modifier = Modifier.padding(top = 4.dp).fillMaxWidth().heightIn(max = 300.dp).hardShadow().border(2.dp, BrutalistBlack), color = BrutalistWhite) {
                    LazyColumn {
                        items(suggestions) { suggestion ->
                            ListItem(headlineContent = { Text(suggestion.displayName, fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Medium) }, modifier = Modifier.clickable { viewModel.selectLocationFromSuggestion(suggestion); searchQuery = suggestion.displayName; viewModel.clearAddressSuggestions() })
                            HorizontalDivider(color = BrutalistBlack.copy(alpha = 0.1f))
                        }
                    }
                }
            }
        }

        selectedLocation?.let { picked ->
            Box(modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp).navigationBarsPadding()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).hardShadow().border(2.dp, BrutalistBlack), color = BrutalistWhite) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.map_label_selected), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp, color = Color(0xFF71717A))
                            Text(picked.displayName, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BrutalistBlack, maxLines = 2)
                        }
                    }
                    Button(
                        onClick = { onLocationConfirmed(picked) }, enabled = !isResolving, modifier = Modifier.fillMaxWidth().height(56.dp).hardShadow().border(2.dp, BrutalistBlack),
                        colors = ButtonDefaults.buttonColors(containerColor = if (isResolving) Color(0xFFD4D4D8) else NeonGreen), shape = MaterialTheme.shapes.extraSmall, contentPadding = PaddingValues(0.dp)
                    ) {
                        if (isResolving) { CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BrutalistBlack, strokeWidth = 2.dp) }
                        else { Text(stringResource(R.string.btn_confirm_address), color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                    }
                }
            }
        }
    }
}
