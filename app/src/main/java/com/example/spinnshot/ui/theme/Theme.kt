package com.example.spinnshot.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpinnShotColors = darkColorScheme(
    primary = NeonFuchsia,
    onPrimary = NightBlack,
    secondary = NeonPurple,
    onSecondary = TextPrimary,
    tertiary = NeonCyan,
    onTertiary = NightBlack,
    background = NightBlack,
    onBackground = TextPrimary,
    surface = NightCard,
    onSurface = TextPrimary,
    surfaceVariant = NightSurface,
    onSurfaceVariant = TextSecondary,
    outline = BorderDim,
    error = NeonFuchsia,
    onError = NightBlack
)

@Composable
fun SpinnShotTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NightBlack.toArgb()
            window.navigationBarColor = NightBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = SpinnShotColors,
        typography = Typography,
        content = content
    )
}
