package com.slior.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.slior.R

//  Tipografía: Space Grotesk via Google Fonts 
// Si el proveedor no está disponible (sin Play Services o cert incorrecto),
// recae automáticamente en la fuente del sistema sin crashear la app.
private val googleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs
)

val SpaceGroteskFamily = FontFamily(
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = GoogleFont("Space Grotesk"), fontProvider = googleFontsProvider, weight = FontWeight.Black),
)

//  Tipografía Material3 usando Space Grotesk 
private val SliorTypography = Typography(
    displayLarge  = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black,     fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black,     fontSize = 45.sp),
    displaySmall  = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black,     fontSize = 32.sp),
    headlineMedium= TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    titleMedium   = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleSmall    = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Medium,    fontSize = 14.sp),
    bodyLarge     = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Normal,    fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Normal,    fontSize = 14.sp),
    bodySmall     = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Normal,    fontSize = 12.sp),
    labelLarge    = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 12.sp),
    labelSmall    = TextStyle(fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold,      fontSize = 11.sp),
)

//  Esquemas de color 
private val LightColors = lightColorScheme(
    primary = BrutalistBlack,
    onPrimary = BrutalistWhite,
    primaryContainer = NeonGreen,
    secondary = SafetyOrange,
    onSecondary = BrutalistWhite,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = BrutalistBlack,
    error = OfflineRed
)

private val DarkColors = darkColorScheme(
    primary = BrutalistWhite,
    onPrimary = BrutalistBlack,
    primaryContainer = NeonGreen,
    secondary = SafetyOrange,
    onSecondary = BrutalistBlack,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = BrutalistWhite,
    error = OfflineRed
)

@Composable
fun SliorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColors
        else -> LightColors
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SliorTypography,
        content = content
    )
}