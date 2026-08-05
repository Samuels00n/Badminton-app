package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = OliveAccent,
    onPrimary = Color(0xFF1A1C19),
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = Color(0xFFF1F3E9),
    secondary = OliveMedium,
    onSecondary = Color.White,
    tertiary = NaturalWarmChip,
    background = DarkNaturalBackground,
    surface = DarkNaturalSurface,
    surfaceVariant = DarkNaturalSurfaceVariant,
    onBackground = Color(0xFFF1F3E9),
    onSurface = Color(0xFFF1F3E9),
    outline = Color(0xFF384B3C),
    error = CoralRedLoss
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ForestGreenContainer,
    onPrimaryContainer = ForestGreenOnContainer,
    secondary = OliveAccent,
    onSecondary = Color(0xFF1A1C19),
    tertiary = OliveMedium,
    background = NaturalBackground,
    surface = NaturalSurface,
    surfaceVariant = NaturalSurfaceVariant,
    onBackground = NaturalTextDark,
    onSurface = NaturalTextDark,
    outline = NaturalCardBorder,
    error = CoralRedLoss
)

@Composable
fun BadmintonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
