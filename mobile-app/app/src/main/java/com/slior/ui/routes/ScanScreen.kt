package com.slior.ui.routes

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.slior.R
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*
import com.slior.util.Result
import androidx.compose.ui.graphics.graphicsLayer

enum class ScanMode {
    QR, BARCODE
}

/**
 * Pantalla de Escaneo de Paquetes.
 * Permite validar códigos QR y de barras.
 * Soporta temas dinámicos (Claro/Oscuro) y modo Landscape.
 */
@Composable
fun ScanScreen(
    stopId: String,
    onBack: () -> Unit,
    onDeliveryConfirmed: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val stop by viewModel.stopDetail.collectAsStateWithLifecycle()
    val scannedCode by viewModel.scannedPackageCode.collectAsStateWithLifecycle()
    val isValid by viewModel.isPackageValid.collectAsStateWithLifecycle()
    val deliveryState by viewModel.deliveryState.collectAsStateWithLifecycle()

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // Colores dinámicos
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var scanMode by remember { mutableStateOf(ScanMode.QR) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // Configuración del escáner
    val barcodeView = remember {
        CompoundBarcodeView(context).apply {
            val callback = com.journeyapps.barcodescanner.BarcodeCallback { result ->
                result.text?.let { viewModel.onPackageScanned(it) }
            }
            this.barcodeView.cameraSettings.isExposureEnabled = true
            this.barcodeView.cameraSettings.isAutoFocusEnabled = true
            this.setStatusText("") // Ocultamos el texto por defecto para que no se superponga
            decodeContinuous(callback)
        }
    }

    LaunchedEffect(scanMode) {
        val formats = if (scanMode == ScanMode.QR) {
            listOf(BarcodeFormat.QR_CODE)
        } else {
            listOf(BarcodeFormat.CODE_128, BarcodeFormat.CODE_39, BarcodeFormat.CODE_93, BarcodeFormat.ITF, BarcodeFormat.EAN_8, BarcodeFormat.EAN_13)
        }
        val hints = mutableMapOf<DecodeHintType, Any>()
        hints[DecodeHintType.TRY_HARDER] = true
        hints[DecodeHintType.POSSIBLE_FORMATS] = formats
        barcodeView.barcodeView.decoderFactory = DefaultDecoderFactory(formats, hints, null, 0)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> barcodeView.resume()
                Lifecycle.Event.ON_PAUSE -> barcodeView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            barcodeView.pause()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
        viewModel.loadStopDetail(stopId)
    }

    LaunchedEffect(deliveryState) {
        if (deliveryState is Result.Success) {
            viewModel.resetDeliveryState()
            onDeliveryConfirmed()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Dinámico
            Surface(
                modifier = Modifier.fillMaxWidth().height(if (isLandscape) 56.dp else 64.dp),
                color = surfaceColor, shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawLine(BrutalistBlack, Offset(0f, size.height - strokeWidth/2), Offset(size.width, size.height - strokeWidth/2), strokeWidth)
                    }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = onSurfaceColor)
                    }
                    Text(
                        text = stringResource(R.string.btn_scan_package),
                        modifier = Modifier.weight(1f),
                        fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = if (isLandscape) 16.sp else 18.sp, color = onSurfaceColor
                    )
                }
            }

            if (!hasCameraPermission) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.perm_required_title), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = SafetyOrange)
                }
            } else {
                if (isLandscape) {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .fillMaxHeight()
                                .background(Color.Black)
                                .border(2.dp, BrutalistBlack)
                                .clip(RectangleShape)
                        ) {
                            AndroidView(factory = { barcodeView }, modifier = Modifier.fillMaxSize())
                            ScannerOverlay(scanMode = scanMode, onToggleMode = {
                                scanMode = if (scanMode == ScanMode.QR) ScanMode.BARCODE else ScanMode.QR
                            }, isLandscape = true, surfaceColor = surfaceColor, onSurfaceColor = onSurfaceColor)
                        }
                        
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ScanInfoContent(stop, scannedCode, isValid, onSurfaceColor, surfaceColor)
                            Spacer(Modifier.weight(1f))
                            ConfirmButton(isValid, deliveryState, viewModel, stopId)
                        }
                    }
                } else {
                    Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        Box(modifier = Modifier.weight(1.2f).fillMaxWidth().background(Color.Black).border(bottom = 4.dp, color = BrutalistBlack)) {
                            AndroidView(factory = { barcodeView }, modifier = Modifier.fillMaxSize())
                            ScannerOverlay(scanMode = scanMode, onToggleMode = {
                                scanMode = if (scanMode == ScanMode.QR) ScanMode.BARCODE else ScanMode.QR
                            }, isLandscape = false, surfaceColor = surfaceColor, onSurfaceColor = onSurfaceColor)
                        }
                        Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ScanInfoContent(stop, scannedCode, isValid, onSurfaceColor, surfaceColor)
                            Spacer(Modifier.weight(1f))
                            ConfirmButton(isValid, deliveryState, viewModel, stopId)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScannerOverlay(scanMode: ScanMode, onToggleMode: () -> Unit, isLandscape: Boolean, surfaceColor: Color, onSurfaceColor: Color) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight
        
        val frameWidth: androidx.compose.ui.unit.Dp
        val frameHeight: androidx.compose.ui.unit.Dp
        
        if (scanMode == ScanMode.QR) {
            val side = if (containerWidth < containerHeight) containerWidth * 0.7f else containerHeight * 0.7f
            frameWidth = side
            frameHeight = side
        } else {
            val targetWidth = containerWidth * 0.8f
            val targetHeight = targetWidth / 3f
            if (targetHeight > containerHeight * 0.4f) {
                frameHeight = containerHeight * 0.4f
                frameWidth = frameHeight * 3f
            } else {
                frameWidth = targetWidth
                frameHeight = targetHeight
            }
        }
        
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(width = frameWidth, height = frameHeight)
                .border(3.dp, NeonGreen)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "laser")
            val laserPosition by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.9f,
                animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
                label = "laserPos"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.01f)
                    .align(Alignment.TopCenter)
                    .graphicsLayer(translationY = 0f)
                    .drawBehind {
                        val y = size.height * laserPosition * (1f / 0.01f)
                        drawLine(NeonGreen, Offset(0f, y), Offset(size.width, y), 2.dp.toPx())
                    }
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isLandscape) 8.dp else if (scanMode == ScanMode.QR) 12.dp else 24.dp)
                .size(if (isLandscape) 40.dp else 56.dp)
                .border(2.dp, NeonGreen, CircleShape)
                .clickable { onToggleMode() },
            color = Color.Transparent,
            shape = CircleShape
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (scanMode == ScanMode.QR) Icons.Default.ViewColumn else Icons.Default.QrCode,
                    contentDescription = "Cambiar modo",
                    tint = NeonGreen,
                    modifier = Modifier.size(if (isLandscape) 24.dp else 32.dp)
                )
            }
        }
        
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
                .border(1.dp, NeonGreen)
                .padding(horizontal = 12.dp, vertical = 4.dp),
            color = Color.Transparent
        ) {
            Text(
                text = if (scanMode == ScanMode.QR) stringResource(R.string.scan_mode_qr) else stringResource(R.string.scan_mode_barcode),
                color = NeonGreen,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun ScanInfoContent(stop: com.slior.data.local.entity.StopEntity?, scannedCode: String?, isValid: Boolean?, onSurfaceColor: Color, surfaceColor: Color) {
    stop?.let { s ->
        Text(stringResource(R.string.label_scan_instructions).uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.5f))
        Text(s.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurfaceColor, maxLines = 1)
    }

    if (scannedCode != null) {
        Surface(
            modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack),
            color = if (isValid == true) NeonGreen.copy(alpha = 0.15f) else SafetyOrange.copy(alpha = 0.15f)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isValid == true) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isValid == true) NeonGreen else SafetyOrange,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("ID:", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 9.sp, color = onSurfaceColor)
                    Text(scannedCode, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 14.sp, maxLines = 1, color = onSurfaceColor)
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxWidth().height(50.dp).border(2.dp, BrutalistBlack, RectangleShape).background(onSurfaceColor.copy(alpha = 0.05f)), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.status_verifying), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = onSurfaceColor.copy(alpha = 0.4f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ConfirmButton(isValid: Boolean?, deliveryState: Result<Unit>?, viewModel: RouteViewModel, stopId: String) {
    Button(
        onClick = { viewModel.confirmDelivery(stopId) },
        enabled = isValid == true && deliveryState !is Result.Loading,
        modifier = Modifier.fillMaxWidth().height(64.dp).hardShadow().border(2.dp, BrutalistBlack),
        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, disabledContainerColor = Color.Gray.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.extraSmall
    ) {
        if (deliveryState is Result.Loading) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
        } else {
            Text(stringResource(R.string.btn_confirm_delivery), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
        }
    }
}

private fun Modifier.border(bottom: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape = RectangleShape) = this.drawBehind {
    val strokeWidth = bottom.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
}
