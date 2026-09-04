package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Bengal Green Brand Colors
val BengalGreen = Color(0xFF006A4E)
val BengalGreenDark = Color(0xFF004D38)
val BengalGreenLight = Color(0xFF0A7A57)
val BengalGreenBright = Color(0xFF15B377)

// Sunrise Red - Used ONLY for logo red dot, notification dots, error states
val SunriseRed = Color(0xFFF42A41)
val SunriseRedLight = Color(0xFFFFEBEE)
val SunriseRedBorder = Color(0xFFFFCDD2)

// Light Theme Palette (Clean editorial canvas matching reference screenshot)
val BgLight = Color(0xFFFAFAFA)
val SurfaceLight = Color(0xFFFFFFFF)
val BorderLight = Color(0xFFE5E7EB)
val TextPrimaryLight = Color(0xFF111827)
val TextMutedLight = Color(0xFF6B7280)
val CardLight = Color(0xFFFFFFFF)

// Dark Theme Palette
val BgDark = Color(0xFF111614)
val SurfaceDark = Color(0xFF19201C)
val BorderDark = Color(0xFF27322C)
val TextPrimaryDark = Color(0xFFF3F4F6)
val TextMutedDark = Color(0xFF9CA3AF)
val CardDark = Color(0xFF1F2723)

// CTA Gradient & Bubble Gradient
val CtaGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0A7A57), Color(0xFF15B377))
)

val UserBubbleGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0A7A57), Color(0xFF15B377))
)

val SplashGradient = Brush.radialGradient(
    colors = listOf(Color(0xFF0A7A57), Color(0xFF006A4E), Color(0xFF004432))
)
