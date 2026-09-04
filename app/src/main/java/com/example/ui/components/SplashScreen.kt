package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BengalGreen
import com.example.ui.theme.SunriseRed
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    // 2D Abstract Rising Sun Animation Progress
    val sunRiseProgress = remember { Animatable(0f) } // 0f (below horizon) to 1f (centered)
    val rayAlpha = remember { Animatable(0f) }
    val badgeMorphProgress = remember { Animatable(0f) } // 0f (sun) to 1f (app icon badge)
    val textAlpha = remember { Animatable(0f) }

    // Subtle continuous rotation for 2D abstract geometric rays
    val infiniteTransition = rememberInfiniteTransition(label = "raysRotation")
    val continuousRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        // Step 1: Sun rises from behind 2D abstract horizon with expanding geometric rays
        rayAlpha.animateTo(1f, tween(800, easing = LinearEasing))
        sunRiseProgress.animateTo(1f, tween(1400, easing = FastOutSlowInEasing))

        // Step 2: Smoothly morphs into the abstract squircle app icon badge
        delay(150)
        badgeMorphProgress.animateTo(1f, tween(750, easing = FastOutSlowInEasing))

        // Step 3: Reveal elegant branding typography
        textAlpha.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
    }

    // 2D Minimalist Deep Forest / Midnight Canvas Gradient
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0D1412),
            Color(0xFF131F1B),
            Color(0xFF192A24)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Background 2D Abstract Horizon Curves & Geometric Sun Canvas
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Calculate center of rising sun in 2D space
            // Rises from (height * 0.70) up to (height * 0.44)
            val startY = height * 0.70f
            val targetY = height * 0.44f
            val currentSunY = startY - ((startY - targetY) * sunRiseProgress.value)
            val sunCenter = Offset(width / 2f, currentSunY)

            // 1. Draw 2D Abstract Radiating Geometric Rays (Pure vector, no photo)
            val numRays = 12
            val innerRayRadius = 48.dp.toPx()
            val outerRayRadius = 110.dp.toPx() * sunRiseProgress.value
            val currentRayAlpha = rayAlpha.value * (1f - (badgeMorphProgress.value * 0.75f))

            if (currentRayAlpha > 0.01f && outerRayRadius > innerRayRadius) {
                rotate(continuousRotation, pivot = sunCenter) {
                    for (i in 0 until numRays) {
                        val angle = (i * (360f / numRays)) * (Math.PI / 180f).toFloat()
                        val cosA = cos(angle)
                        val sinA = sin(angle)

                        val startPoint = Offset(
                            x = sunCenter.x + (cosA * innerRayRadius),
                            y = sunCenter.y + (sinA * innerRayRadius)
                        )
                        val endPoint = Offset(
                            x = sunCenter.x + (cosA * outerRayRadius),
                            y = sunCenter.y + (sinA * outerRayRadius)
                        )

                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    SunriseRed.copy(alpha = 0.55f * currentRayAlpha),
                                    Color(0xFFFF7043).copy(alpha = 0.25f * currentRayAlpha),
                                    Color.Transparent
                                ),
                                start = startPoint,
                                end = endPoint
                            ),
                            start = startPoint,
                            end = endPoint,
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // 2D Concentric Corona Waves
                drawCircle(
                    color = SunriseRed.copy(alpha = 0.18f * currentRayAlpha),
                    radius = (58.dp.toPx() + (20.dp.toPx() * sunRiseProgress.value)),
                    center = sunCenter,
                    style = Stroke(width = 1.5.dp.toPx())
                )
                drawCircle(
                    color = Color(0xFFFF5252).copy(alpha = 0.10f * currentRayAlpha),
                    radius = (78.dp.toPx() + (30.dp.toPx() * sunRiseProgress.value)),
                    center = sunCenter,
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // 2. Draw 2D Stylized Horizon Landscape (Gentle abstract landscape contours)
            // Fades out gently as badge forms
            val horizonAlpha = (1f - (badgeMorphProgress.value * 0.85f)) * sunRiseProgress.value
            if (horizonAlpha > 0.01f) {
                // Secondary background hill contour
                val hillPath2 = Path().apply {
                    moveTo(0f, height * 0.67f)
                    cubicTo(
                        width * 0.35f, height * 0.63f,
                        width * 0.70f, height * 0.71f,
                        width, height * 0.66f
                    )
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = hillPath2,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF162520).copy(alpha = 0.85f * horizonAlpha),
                            Color(0xFF0F1A16).copy(alpha = horizonAlpha)
                        )
                    )
                )

                // Foreground horizon contour
                val hillPath1 = Path().apply {
                    moveTo(0f, height * 0.71f)
                    cubicTo(
                        width * 0.30f, height * 0.73f,
                        width * 0.65f, height * 0.67f,
                        width, height * 0.72f
                    )
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(
                    path = hillPath1,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BengalGreen.copy(alpha = 0.75f * horizonAlpha),
                            Color(0xFF0D1714).copy(alpha = horizonAlpha)
                        )
                    )
                )
            }
        }

        // Center Content Column: 2D Sun Disk morphing to App Icon Badge + Typography
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Main 2D Centerpiece
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(100.dp)
            ) {
                // 2D Abstract Rising Sun Disk (fades into the badge)
                if (badgeMorphProgress.value < 0.95f) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(0.85f + (sunRiseProgress.value * 0.15f))
                            .alpha(1f - (badgeMorphProgress.value * 0.9f))
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFFFF5252),
                                        SunriseRed,
                                        Color(0xFFB71C1C)
                                    )
                                )
                            )
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFFFF8A80).copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                    )
                }

                // App Icon Badge (morphs in as the final identity)
                if (badgeMorphProgress.value > 0.05f) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_logo),
                        contentDescription = "Bondhu App Logo",
                        modifier = Modifier
                            .size(76.dp)
                            .scale(0.9f + (badgeMorphProgress.value * 0.1f))
                            .alpha(badgeMorphProgress.value)
                            .shadow(
                                elevation = 16.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = SunriseRed.copy(alpha = 0.35f)
                            )
                            .clip(RoundedCornerShape(20.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Branding Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(textAlpha.value)
            ) {
                Text(
                    text = "Bondhu AI",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "What can I build for you?",
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFC8D3CE),
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}
