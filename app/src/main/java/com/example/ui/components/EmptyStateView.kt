package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.NotoSansBengaliFamily

private data class RecommendationItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

/**
 * Empty State View:
 * Sleek, clean and un-bloated greeting:
 * - Warm personalized welcome ("Welcome, [name]!")
 * - Compact, sleek, unbloated prompt recommendation pills
 */
@Composable
fun EmptyStateView(
    userName: String,
    language: String,
    onSuggestionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val displayName = userName.trim().ifBlank {
        if (language == "bn") "বন্ধু" else "Friend"
    }

    // Subtle gentle glow pulse for the AI ready badge
    val infiniteTransition = rememberInfiniteTransition(label = "badge_pulse")
    val badgeGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badge_glow"
    )

    // Compact, sleek recommendation chips (not bloated!)
    val recommendations = listOf(
        RecommendationItem(
            id = "Today's news",
            title = if (language == "bn") "আজকের খবর" else "Today's news",
            icon = Icons.Outlined.Newspaper
        ),
        RecommendationItem(
            id = "Math help",
            title = if (language == "bn") "গণিত সমাধান" else "Math help",
            icon = Icons.Outlined.Calculate
        ),
        RecommendationItem(
            id = "Write a story",
            title = if (language == "bn") "গল্প লেখো" else "Write a story",
            icon = Icons.Outlined.EditNote
        ),
        RecommendationItem(
            id = "Learn English",
            title = if (language == "bn") "ইংরেজি শিখি" else "Learn English",
            icon = Icons.Outlined.Translate
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Subtle AI Status Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(colors.primary.copy(alpha = badgeGlowAlpha))
                .border(
                    width = 1.dp,
                    color = colors.primary.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == "bn") "বন্ধু এআই প্রস্তুত" else "Bondhu AI Ready",
                    fontFamily = NotoSansBengaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Large personalized Welcome Title: "Welcome, [Name]!" / "স্বাগতম, [Name]!"
        Text(
            text = if (language == "bn") "স্বাগতম, $displayName!" else "Welcome, $displayName!",
            fontFamily = NotoSansBengaliFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Futuristic friendly tagline
        Text(
            text = if (language == "bn") "আমি বন্ধু — আপনার এআই সঙ্গী। কী জানতে চান?" else "I'm Bondhu — your AI companion. How can I help you?",
            fontFamily = NotoSansBengaliFamily,
            fontSize = 14.5.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Sleek, compact, un-bloated recommendation prompt pills
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Row 1 (2 sleek chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                recommendations.take(2).forEach { chip ->
                    RecommendationPill(
                        chip = chip,
                        onClick = { onSuggestionClick(Strings.suggestionPrompt(chip.id, language)) },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            // Row 2 (2 sleek chips)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                recommendations.drop(2).take(2).forEach { chip ->
                    RecommendationPill(
                        chip = chip,
                        onClick = { onSuggestionClick(Strings.suggestionPrompt(chip.id, language)) },
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

/**
 * Compact, lightweight recommendation pill (no bloat)
 */
@Composable
private fun RecommendationPill(
    chip: RecommendationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("rec_pill_${chip.id}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = chip.icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = chip.title,
                fontFamily = NotoSansBengaliFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary,
                maxLines = 1
            )
        }
    }
}


