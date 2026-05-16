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
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.ui.components.SliorAccentBar
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*

/**
 * Pantalla de Ajustes de la aplicación.
 * Permite cambiar el idioma y el tema visual.
 * Soporta Modo Oscuro y reinicio dinámico.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: com.slior.viewmodel.AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    
    // Colores dinámicos del tema
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Estado para el idioma que se quiere seleccionar (pendiente de confirmar)
    var pendingLocaleCode by remember { mutableStateOf<String?>(null) }
    
    // Obtener idioma actual
    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "es"

    // Diálogo de confirmación brutalista para idioma
    if (pendingLocaleCode != null) {
        AlertDialog(
            onDismissRequest = { pendingLocaleCode = null },
            containerColor = surfaceColor,
            shape = MaterialTheme.shapes.extraSmall,
            tonalElevation = 0.dp,
            modifier = Modifier.border(2.dp, BrutalistBlack),
            title = {
                Text(
                    text = stringResource(R.string.dialog_lang_title),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = onSurfaceColor
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_lang_desc),
                    fontFamily = SpaceGroteskFamily,
                    color = onSurfaceColor
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .border(2.dp, BrutalistBlack)
                        .background(NeonGreen)
                        .clickable { 
                            val appLocales: LocaleListCompat = LocaleListCompat.forLanguageTags(pendingLocaleCode!!)
                            AppCompatDelegate.setApplicationLocales(appLocales)
                            pendingLocaleCode = null
                            
                            // Forzar reinicio de Activity
                            (context as? android.app.Activity)?.recreate()
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
                        color = onSurfaceColor,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar Brutalista
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = surfaceColor,
                shape = RectangleShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind { 
                            drawLine(
                                color = onSurfaceColor, 
                                start = Offset(0f, size.height), 
                                end = Offset(size.width, size.height), 
                                strokeWidth = 2.dp.toPx()
                            ) 
                        }
                        .padding(horizontal = 8.dp),
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
                            contentDescription = stringResource(R.string.btn_back), 
                            tint = onSurfaceColor, 
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.menu_settings),
                        fontFamily = SpaceGroteskFamily, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 20.sp, 
                        color = onSurfaceColor
                    )
                }
            }

            SliorAccentBar()

            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sección Tema
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.DarkMode, null, tint = onSurfaceColor)
                        Text(
                            text = stringResource(R.string.title_theme),
                            fontFamily = SpaceGroteskFamily, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 12.sp, 
                            letterSpacing = 2.sp, 
                            color = onSurfaceColor
                        )
                    }

                    ThemeOption(
                        label = stringResource(R.string.theme_light),
                        isSelected = currentTheme == "light",
                        onSurfaceColor = onSurfaceColor,
                        surfaceColor = surfaceColor,
                        onClick = { 
                            viewModel.saveTheme("light")
                        }
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_dark),
                        isSelected = currentTheme == "dark",
                        onSurfaceColor = onSurfaceColor,
                        surfaceColor = surfaceColor,
                        onClick = { 
                            viewModel.saveTheme("dark")
                        }
                    )
                    ThemeOption(
                        label = stringResource(R.string.theme_system),
                        isSelected = currentTheme == "system",
                        onSurfaceColor = onSurfaceColor,
                        surfaceColor = surfaceColor,
                        onClick = { 
                            viewModel.saveTheme("system")
                        }
                    )
                }

                // Sección Idioma
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Language, null, tint = onSurfaceColor)
                        Text(
                            text = "IDIOMA / LANGUAGE",
                            fontFamily = SpaceGroteskFamily, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 12.sp, 
                            letterSpacing = 2.sp, 
                            color = onSurfaceColor
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
                            onSurfaceColor = onSurfaceColor,
                            surfaceColor = surfaceColor,
                            onClick = {
                                if (currentLocale != code) {
                                    pendingLocaleCode = code
                                }
                            }
                        )
                    }
                }
                
                // Footer decorativo
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = stringResource(R.string.menu_version_info), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.5f))
                    Text(text = "SLIOR TFG — 2026", fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    isSelected: Boolean,
    onSurfaceColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrutalistBlack)
            .background(if (isSelected) NeonGreen else surfaceColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, BrutalistBlack)
                .background(if (isSelected) BrutalistBlack else surfaceColor),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text("✓", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isSelected) BrutalistBlack else onSurfaceColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onSurfaceColor: Color,
    surfaceColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrutalistBlack)
            .background(if (isSelected) NeonGreen else surfaceColor)
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .border(2.dp, BrutalistBlack)
                .background(if (isSelected) BrutalistBlack else surfaceColor),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Text("✓", color = NeonGreen, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = label,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isSelected) BrutalistBlack else onSurfaceColor,
            modifier = Modifier.weight(1f)
        )
    }
}
