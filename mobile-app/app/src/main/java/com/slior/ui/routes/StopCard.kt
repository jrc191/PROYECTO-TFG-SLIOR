package com.slior.ui.routes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slior.data.local.entity.StopEntity
import com.slior.ui.theme.SpaceGroteskFamily
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SafetyOrange

@Composable
fun StopCard(
    stop: StopEntity,
    onStatusChange: (String) -> Unit
) {
    var isCompleted by remember { mutableStateOf(stop.status.uppercase() == "ENTREGADO") }
    val isActive = stop.status.uppercase() == "EN_CAMINO"
    val statusColor = if (isCompleted) NeonGreen else if (isActive) SafetyOrange else BrutalistWhite

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            // MODIFICADOR CORREGIDO: sombra -> fondo -> borde
            .hardShadow(
                offsetX = 4.dp,
                offsetY = 4.dp
            )
            .background(BrutalistWhite)
            .border(
                width = 2.dp, 
                color = BrutalistBlack
            )
            .clickable {
                if (!isActive) {
                    isCompleted = !isCompleted
                    val newStatus = if (isCompleted) "ENTREGADO" else "PENDIENTE"
                    onStatusChange(newStatus)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Indicador de estado (checkbox o check)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(statusColor)
                    .border(
                        width = 2.dp, 
                        color = BrutalistBlack
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = BrutalistBlack,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Contenido de la parada
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stop.destinatario.uppercase(),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = BrutalistBlack,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                )

                Text(
                    text = "ENTREGA #${stop.id.takeLast(4)}",
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.alpha(if (isCompleted) 0.6f else 1f)
                )
            }

            // Badge "AHORA" si está activa
            if (isActive) {
                Box(
                    modifier = Modifier
                        .background(SafetyOrange)
                        .border(
                            width = 2.dp, 
                            color = BrutalistBlack
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AHORA",
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = BrutalistBlack
                    )
                }
            }
        }
    }
}
