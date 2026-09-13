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
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.HindSiliguriFamily
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
    val scale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        // Smooth, gentle fade-in and scale
        alpha.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        // Brief comfortable hold
        delay(900)
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
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
        ) {
            // Official Splash Logo Image
            Image(
                painter = painterResource(R.drawable.img_app_logo),
                contentDescription = "Bondhu AI Logo",
                modifier = Modifier
                    .size(110.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("splash_logo_image")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "বন্ধু" logo
            BondhuLogo(
                size = LogoSize.LARGE,
                showAi = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (language == "bn") "বাংলাদেশের নিজস্ব এআই" else "Bangladesh's Own AI Companion",
                fontFamily = HindSiliguriFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        }
    }
}
