package com.brainspresso.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// A deliberately bold, curated palette -- no Material You dynamic color here, since the
// whole point of the retro-alarm-clock look is a specific identity, not whatever colors
// happen to match the device wallpaper.
private val LightColors = lightColorScheme(
    primary = RetroRed,
    onPrimary = RetroCream,
    secondary = RetroAmber,
    onSecondary = RetroInk,
    tertiary = RetroTeal,
    onTertiary = RetroCream,
    background = RetroCream,
    onBackground = RetroInk,
    surface = RetroCreamSurface,
    onSurface = RetroInk,
    surfaceVariant = RetroMustardSurface,
    onSurfaceVariant = RetroInk,
    outline = RetroInk,
    error = RetroMaroon,
    onError = RetroCream,
)

private val DarkColors = darkColorScheme(
    primary = RetroRedBright,
    onPrimary = RetroInk,
    secondary = RetroAmber,
    onSecondary = RetroInk,
    tertiary = RetroTealBright,
    onTertiary = RetroInk,
    background = RetroCharcoal,
    onBackground = RetroCreamText,
    surface = RetroCharcoalSurface,
    onSurface = RetroCreamText,
    surfaceVariant = RetroBrownSurface,
    onSurfaceVariant = RetroCreamText,
    outline = RetroCreamText,
    error = RetroErrorBright,
    onError = RetroInk,
)

@Composable
fun BrainspressoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BrainspressoTypography,
        shapes = BrainspressoShapes,
        content = content,
    )
}
