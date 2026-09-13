package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// SPECIFIED SHAPES: Buttons 12dp, Cards/panels 16dp, Composer 24dp
val BondhuShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Dark Theme (DEFAULT)
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGreenNeon,
    onPrimary = Color(0xFF003915),
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = DarkTextPrimary,
    secondary = PrimaryGreenNeon,
    onSecondary = DarkTextPrimary,
    secondaryContainer = DarkSurface,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = BrandGold,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = ErrorDestructive,
    onError = DarkTextPrimary
)

// Light Theme (Optional)
private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceElevated,
    onPrimaryContainer = LightText,
    secondary = LightPrimary,
    onSecondary = Color.White,
    secondaryContainer = LightSurface,
    onSecondaryContainer = LightText,
    tertiary = LightSolarAccent,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurfaceRaised,
    onSurfaceVariant = LightMuted,
    outline = LightBorder,
    outlineVariant = LightBorder,
    error = LightError,
    onError = Color.White
)

object BondhuTheme {
    val colors: BondhuColorPalette
        @Composable
        @ReadOnlyComposable
        get() = LocalBondhuColors.current
}

@Composable
fun BondhuTheme(
    themeMode: String = "dark", // Dark theme is default
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val isEffectiveDark = when (themeMode) {
        "light" -> false
        "dark" -> true
        "system" -> isSystemInDarkTheme()
        else -> true // Default to pure black dark theme
    }
    val colorScheme = if (isEffectiveDark) DarkColorScheme else LightColorScheme
    val bondhuPalette = if (isEffectiveDark) DarkPalette else LightPalette

    CompositionLocalProvider(LocalBondhuColors provides bondhuPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = BondhuShapes,
            content = content
        )
    }
}


