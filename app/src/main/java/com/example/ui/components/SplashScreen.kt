package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.NotoSansBengaliFamily
import kotlinx.coroutines.delay

/**
 * SCREEN 1: SPLASH
 * Minimalist, fast & authentic:
 * Normal "বন্ধু AI" wordmark in Bangla with smooth fade-in, brief hold, and fade-out.
 * Tap anywhere immediately skips without waiting.
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
        // 1. Smooth Fade In
        alpha.animateTo(1f, animationSpec = tween(350, easing = FastOutSlowInEasing))
        // 2. Comfortable short hold
        delay(450)
        // 3. Smooth Fade Out
        alpha.animateTo(0f, animationSpec = tween(250, easing = LinearOutSlowInEasing))
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
            // Normal "বন্ধু AI" in Bangla
            BondhuLogo(
                size = LogoSize.SPLASH,
                showAi = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = if (language == "bn") "বাংলাদেশের নিজস্ব এআই" else "Bangladesh's Own AI Companion",
                fontFamily = NotoSansBengaliFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        }
    }
}
