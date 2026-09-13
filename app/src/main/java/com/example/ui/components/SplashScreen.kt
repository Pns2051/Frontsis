package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.BondhuTheme
import kotlinx.coroutines.delay

/**
 * SCREEN 1: SPLASH
 * Normal, easy, and clean:
 * Centered "বন্ধু" logo with a smooth, gentle fade-in.
 * Tap anywhere immediately advances.
 */
@Composable
fun SplashScreen(
    language: String,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Smooth, gentle fade-in
        alpha.animateTo(1f, animationSpec = tween(400))
        // Brief comfortable hold
        delay(700)
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(alpha.value)
        ) {
            // "বন্ধু" (only the 'bo' as requested)
            BondhuLogo(
                size = LogoSize.SPLASH,
                showAi = false
            )
        }
    }
}
