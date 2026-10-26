package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = SpaceDarkBackground,
    primaryContainer = DeepViolet,
    onPrimaryContainer = TextWhitePrimary,
    secondary = EmeraldCloneGreen,
    onSecondary = SpaceDarkBackground,
    secondaryContainer = SpaceSurfaceVariant,
    onSecondaryContainer = TextWhitePrimary,
    tertiary = CoralRed,
    background = SpaceDarkBackground,
    onBackground = TextWhitePrimary,
    surface = SpaceSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = SpaceSurfaceVariant,
    onSurfaceVariant = TextGraySecondary,
    outline = SpaceBorderGlass
)

private val LightColorScheme = lightColorScheme(
    primary = DeepViolet,
    onPrimary = TextWhitePrimary,
    secondary = EmeraldCloneGreen,
    onSecondary = TextWhitePrimary,
    tertiary = CoralRed,
    background = SpaceDarkBackground, // Consistent immersive space theme
    onBackground = TextWhitePrimary,
    surface = SpaceSurface,
    onSurface = TextWhitePrimary,
    surfaceVariant = SpaceSurfaceVariant,
    onSurfaceVariant = TextGraySecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to immersive dark dual space
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SpaceDarkBackground.toArgb()
                window.navigationBarColor = SpaceDarkBackground.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
