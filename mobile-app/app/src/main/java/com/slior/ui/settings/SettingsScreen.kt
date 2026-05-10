package com.slior.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.slior.R
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    // Estado para el idioma que se quiere seleccionar (pendiente de confirmar)
    var pendingLocaleCode by remember { mutableStateOf<String?>(null) }
    
    // Obtener idioma actual
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "es"

    // Diálogo de confirmación brutalista
    if (pendingLocaleCode != null) {
        AlertDialog(
            onDismissRequest = { pendingLocaleCode = null },
            containerColor = BrutalistWhite,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = stringResource(R.string.dialog_lang_title),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = BrutalistBlack
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_lang_desc),
                    fontFamily = SpaceGroteskFamily,
                    color = BrutalistBlack
                )
            },
            confirmButton = {
                // Surface envuelto en una Box para asegurar alineación correcta en el AlertDialog
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .border(2.dp, BrutalistBlack)
                        .background(NeonGreen)
                        .clickable { 
                            val appLocales: LocaleListCompat = LocaleListCompat.forLanguageTags(pendingLocaleCode!!)
                            AppCompatDelegate.setApplicationLocales(appLocales)
                            pendingLocaleCode = null
                        }
                ) {
                    Text(
                        text = stringResource(R.string.btn_change),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = BrutalistBlack,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLocaleCode = null }) {
                    Text(
                        text = stringResource(R.string.btn_cancel),
                        color = BrutalistBlack,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(BrutalistWhite)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = BrutalistWhite,
                shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(48.dp).clickable { onBack() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = BrutalistBlack, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.menu_settings),
                        fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BrutalistBlack
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sección Idioma
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Language, null, tint = BrutalistBlack)
                        Text(
                            text = "IDIOMA / LANGUAGE",
                            fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 2.sp, color = BrutalistBlack
                        )
                    }
                    
                    val languages = listOf(
                        "es" to "ESPAÑOL",
                        "en" to "ENGLISH",
                        "fr" to "FRANÇAIS",
                        "pt" to "PORTUGUÊS",
                        "de" to "DEUTSCH"
                    )

                    languages.forEach { (code, label) ->
                        LanguageOption(
                            label = label,
                            isSelected = currentLocale == code,
                            onClick = {
                                if (currentLocale != code) {
                                    pendingLocaleCode = code
                                }
                            }
                        )
                    }
                }
                
                // Otras secciones decorativas
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.menu_version_info), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Color.Gray)
                    Text(text = "SLIOR TFG — 2026", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrutalistBlack)
            .background(if (isSelected) NeonGreen else BrutalistWhite)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Caja de selección (Check)
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, BrutalistBlack)
                .background(if (isSelected) BrutalistBlack else BrutalistWhite),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text(
                    text = "✓",
                    color = NeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BrutalistBlack,
            modifier = Modifier.weight(1f)
        )
    }
}
