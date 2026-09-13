package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ── Dark Theme (Sleek Futuristic Deep Dark) ──
val DarkBackground = Color(0xFF0D0D0D)
val DarkSurface = Color(0xFF131313)
val DarkSurfaceRaised = Color(0xFF161616)
val DarkSurfaceContainerLow = Color(0xFF1C1B1B)
val DarkSurfaceElevated = Color(0xFF201F1F)
val DarkSurfaceContainerHigh = Color(0xFF2A2A2A)
val DarkBorder = Color(0xFF262626)
val DarkDivider = Color(0xFF262626)
val PrimaryGreen = Color(0xFF22C55E)
val PrimaryGreenNeon = Color(0xFF4BE277)
val DarkTextPrimary = Color(0xFFFAFAFA)
val DarkTextSecondary = Color(0xFFA3A3A3)
val DarkTextTertiary = Color(0xFF737373)
val ErrorDestructive = Color(0xFFEF4444)
val SolarAccent = Color(0xFFEAB308) // Lightning & credits gold

// ── Light Theme (Sleek, Clean Modern High-Contrast) ──
val LightBackground = Color(0xFFF8FAF9)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceRaised = Color(0xFFF1F5F2)
val LightSurfaceContainerLow = Color(0xFFF3F4F6)
val LightSurfaceElevated = Color(0xFFE5E7EB)
val LightSurfaceContainerHigh = Color(0xFFD1D5DB)
val LightBorder = Color(0xFFE2E8F0)
val LightDivider = Color(0xFFE5E7EB)
val LightPrimary = Color(0xFF16A34A)
val LightText = Color(0xFF0F172A)
val LightMuted = Color(0xFF64748B)
val LightTextTertiary = Color(0xFF94A3B8)
val LightError = Color(0xFFDC2626)
val LightSolarAccent = Color(0xFFD97706)

// Brand Signature
val BrandGold = SolarAccent

// Convenient Aliases
val PrimaryHover = Color(0xFF16A34A)
val GreenPrimary = PrimaryGreen
val GreenDarkMode = PrimaryGreenNeon
val GreenDeep = PrimaryGreen
val Terracotta = ErrorDestructive
val GoldAccent = BrandGold
val DarkBg = DarkBackground
val SurfaceDark = DarkSurface
val SurfaceVariantDark = DarkSurfaceElevated
val DarkText = DarkTextPrimary
val DarkMuted = DarkTextSecondary
val PaperBg = LightBackground
val SurfaceLight = LightSurface
val InkText = LightText
val MutedText = LightMuted
val BorderLine = LightDivider
val SurfaceBg = SurfaceLight
val DarkCard = SurfaceDark
val BengalGreen = PrimaryGreen
val BengalGreenDark = PrimaryGreen
val SunriseRed = ErrorDestructive
val MustardGold = BrandGold

@Immutable
data class BondhuColorPalette(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceElevated: Color,
    val surfaceContainer: Color,
    val surfaceContainerLow: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val primary: Color,
    val onPrimary: Color,
    val solarAccent: Color,
    val error: Color
)

val DarkPalette = BondhuColorPalette(
    isDark = true,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceRaised = DarkSurfaceRaised,
    surfaceElevated = DarkSurfaceElevated,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerLow = DarkSurfaceContainerLow,
    border = DarkBorder,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    primary = PrimaryGreenNeon,
    onPrimary = Color(0xFF003915),
    solarAccent = SolarAccent,
    error = ErrorDestructive
)

val LightPalette = BondhuColorPalette(
    isDark = false,
    background = LightBackground,
    surface = LightSurface,
    surfaceRaised = LightSurfaceRaised,
    surfaceElevated = LightSurfaceElevated,
    surfaceContainer = LightSurfaceContainerLow,
    surfaceContainerLow = Color(0xFFFAFAFA),
    border = LightBorder,
    textPrimary = LightText,
    textSecondary = LightMuted,
    textTertiary = LightTextTertiary,
    primary = LightPrimary,
    onPrimary = Color.White,
    solarAccent = LightSolarAccent,
    error = LightError
)

val LocalBondhuColors = staticCompositionLocalOf { DarkPalette }


