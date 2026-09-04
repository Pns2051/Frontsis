package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = BengalGreenBright,
    onPrimary = Color.White,
    primaryContainer = BengalGreenDark,
    onPrimaryContainer = Color(0xFFC7F9E5),
    secondary = BengalGreenLight,
    onSecondary = Color.White,
    background = BgDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardDark,
    onSurfaceVariant = TextMutedDark,
    outline = BorderDark,
    error = SunriseRed,
    onError = Color.White,
    errorContainer = Color(0xFF3B1216),
    onErrorContainer = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = BengalGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F5EE),
    onPrimaryContainer = BengalGreenDark,
    secondary = BengalGreenLight,
    onSecondary = Color.White,
    background = BgLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF1F3F1),
    onSurfaceVariant = TextMutedLight,
    outline = BorderLight,
    error = SunriseRed,
    onError = Color.White,
    errorContainer = SunriseRedLight,
    onErrorContainer = Color(0xFF8C0012)
)

@Composable
fun BondhuTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
