package com.brainspresso.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = Espresso,
    onPrimary = CreamSurface,
    secondary = Caramel,
    onSecondary = CocoaBrownText,
    tertiary = Latte,
    onTertiary = CocoaBrownText,
    background = CreamBackground,
    onBackground = CocoaBrownText,
    surface = CreamSurface,
    onSurface = CocoaBrownText,
    surfaceVariant = CaramelLight,
    onSurfaceVariant = CocoaBrownText,
    error = WarmError,
)

private val DarkColors = darkColorScheme(
    primary = CaramelDark,
    onPrimary = CoffeeBean,
    secondary = Caramel,
    onSecondary = CoffeeBean,
    tertiary = Latte,
    onTertiary = CoffeeBean,
    background = CoffeeBean,
    onBackground = CreamOnDark,
    surface = CoffeeBeanSurface,
    onSurface = CreamOnDark,
    surfaceVariant = EspressoDark,
    onSurfaceVariant = CreamOnDark,
    error = WarmErrorDark,
)

@Composable
fun BrainspressoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
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
        content = content,
    )
}
