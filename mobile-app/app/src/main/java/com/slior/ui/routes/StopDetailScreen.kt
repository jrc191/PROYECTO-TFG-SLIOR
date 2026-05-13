package com.slior.ui.routes

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*
import com.slior.util.NavigationHelper

@Composable
fun StopDetailScreen(
    stopId: String,
    onBack: () -> Unit,
    onScan: (String) -> Unit,
    viewModel: RouteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val stop by viewModel.stopDetail.collectAsStateWithLifecycle()
    
    LaunchedEffect(stopId) {
        viewModel.loadStopDetail(stopId)
    }

    Box(modifier = Modifier.fillMaxSize().background(BrutalistWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = BrutalistWhite, shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().border(bottom = 2.dp, color = BrutalistBlack).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = BrutalistBlack)
                    }
                    Text(
                        text = stringResource(R.string.title_route_detail).uppercase(), // Reuse or add new string
                        modifier = Modifier.weight(1f),
                        fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrutalistBlack
                    )
                }
            }

            if (stop == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrutalistBlack)
                }
            } else {
                val s = stop!!
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Estado de entrega
                    if (s.status == "ENTREGADO") {
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(2.dp, BrutalistBlack),
                            color = NeonGreen
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(stringResource(R.string.label_delivery_confirmed), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrutalistBlack)
                            }
                        }
                    }

                    // Información del Destinatario
                    Column {
                        Text(stringResource(R.string.label_recipient_uppercase), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                        Text(s.destinatario.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 24.sp, color = BrutalistBlack)
                    }

                    // Dirección
                    Column {
                        Text(stringResource(R.string.label_address).uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                        Text(s.direccion, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BrutalistBlack)
                    }

                    // Teléfono
                    if (s.telefonoDestinatario.isNotBlank()) {
                        Column {
                            Text(stringResource(R.string.label_phone).uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray, letterSpacing = 1.sp)
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${s.telefonoDestinatario}"))
                                context.startActivity(intent)
                            }) {
                                Text(s.telefonoDestinatario, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SafetyOrange)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Botón Navegar
                    Button(
                        onClick = { NavigationHelper.launchSingleStopNavigation(context, s) },
                        modifier = Modifier.fillMaxWidth().height(64.dp).hardShadow().border(2.dp, BrutalistBlack),
                        colors = ButtonDefaults.buttonColors(containerColor = BrutalistWhite),
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Icon(Icons.Default.Navigation, null, tint = BrutalistBlack)
                        Spacer(Modifier.width(12.dp))
                        Text(stringResource(R.string.btn_navigate_stop), color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }

                    // Botón Escanear (solo si no está entregado)
                    if (s.status != "ENTREGADO") {
                        // INFO DE DESARROLLO (TEMPORAL PARA PRUEBAS)
                        Surface(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color.LightGray),
                            color = Color(0xFFF1F5F9)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("INFO PARA PRUEBAS (SOLO DEV):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("ID de Parada: ${s.id}", fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                Text("Usa este ID en un generador de QR online para probar el escaneo.", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Button(
                            onClick = { onScan(s.id) },
                            modifier = Modifier.fillMaxWidth().height(72.dp).hardShadow().border(2.dp, BrutalistBlack),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            shape = MaterialTheme.shapes.extraSmall
                        ) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = BrutalistBlack, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.btn_scan_package), color = BrutalistBlack, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun Modifier.border(bottom: androidx.compose.ui.unit.Dp, color: Color) = this.drawBehind {
    val strokeWidth = bottom.toPx()
    val y = size.height - strokeWidth / 2
    drawLine(color, Offset(0f, y), Offset(size.width, y), strokeWidth)
}
