package com.slior.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val SpaceGroteskFamily = FontFamily.SansSerif

private val LightColors = lightColorScheme(
    primary = SliorBlue,
    onPrimary = SurfaceLight,
    primaryContainer = SliorBlueLight,
    secondary = SliorOrange,
    onSecondary = SurfaceLight,
    secondaryContainer = SliorOrangeDark,
    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = StatusCancelled
)

private val DarkColors = darkColorScheme(
    primary = SliorBlueLight,
    onPrimary = BackgroundDark,
    primaryContainer = SliorBlue,
    secondary = SliorOrange,
    onSecondary = BackgroundDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = StatusCancelled
)

@Composable
fun SliorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    // Adaptar la status bar al tema actual
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
