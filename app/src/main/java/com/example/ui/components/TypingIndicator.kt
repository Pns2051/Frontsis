package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.HindSiliguriFamily

/**
 * ── THINKING & TYPING ANIMATION ──
 * High-tech, expressive thinking indicator:
 * - Animated neural pulsing AI avatar with rotating sparkle
 * - 3 glowing bouncing wave dots with vertical offset & scale oscillation
 * - Shimmering thinking progress pill with live status
 */
@Composable
fun ThinkingAnimation(
    isColdStart: Boolean = false,
    language: String = "bn",
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "thinking_anim")

    // Rotation of sparkle
    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sparkle_angle"
    )

    // Pulsing neural aura
    val auraScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_scale"
    )

    // Shimmer sweep across pill
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_offset"
    )

    // 3 Bouncing wave dots with staggered vertical offsets
    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1_y"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2_y"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3_y"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("thinking_indicator"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // High-tech Neural Avatar Badge
        Box(
            modifier = Modifier
                .size(30.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing Emerald Aura
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .scale(auraScale)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = 0.2f))
            )

            // Inner Badge
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceElevated)
                    .border(1.2.dp, colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Psychology,
                    contentDescription = "Thinking",
                    tint = colors.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(sparkleAngle * 0.15f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Thinking Card with Wave Dots & Shimmer
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            colors.border,
                            colors.primary.copy(alpha = 0.5f),
                            colors.border
                        ),
                        startX = shimmerOffset * 200f,
                        endX = (shimmerOffset + 1f) * 200f
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 3 Wave Bouncing Dots
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(y = dot1Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.primary)
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot2Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.85f))
                    )
                    Box(
                        modifier = Modifier
                            .offset(y = dot3Offset.dp)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.7f))
                    )
                }

                // Thinking Title with Sparkle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isColdStart) Strings.coldStart(language) else Strings.thinking(language),
                        fontFamily = BalooDa2Family,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.5.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = colors.solarAccent,
                        modifier = Modifier
                            .size(13.dp)
                            .rotate(sparkleAngle)
                    )
                }
            }
        }
    }
}

/**
 * Backward compatible alias for TypingIndicator
 */
@Composable
fun TypingIndicator(
    isColdStart: Boolean = false,
    language: String = "bn",
    modifier: Modifier = Modifier
) {
    ThinkingAnimation(
        isColdStart = isColdStart,
        language = language,
        modifier = modifier
    )
}

/**
 * Inline Thinking Card used inside message rows:
 * Shows 3 bouncing wave dots and shimmer thinking text
 * without duplicate avatar icon or row padding.
 */
@Composable
fun InlineThinkingCard(
    isColdStart: Boolean = false,
    language: String = "bn",
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "inline_thinking_anim")

    val sparkleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inline_sparkle_angle"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inline_shimmer_offset"
    )

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "inline_dot1_y"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 130, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "inline_dot2_y"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 260, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "inline_dot3_y"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        colors.border,
                        colors.primary.copy(alpha = 0.5f),
                        colors.border
                    ),
                    startX = shimmerOffset * 200f,
                    endX = (shimmerOffset + 1f) * 200f
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("inline_thinking_indicator")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 3 Wave Bouncing Dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .offset(y = dot1Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot2Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.85f))
                )
                Box(
                    modifier = Modifier
                        .offset(y = dot3Offset.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.7f))
                )
            }

            // Thinking Title with Sparkle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isColdStart) Strings.coldStart(language) else Strings.thinking(language),
                    fontFamily = BalooDa2Family,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = colors.solarAccent,
                    modifier = Modifier
                        .size(13.dp)
                        .rotate(sparkleAngle)
                )
            }
        }
    }
}

