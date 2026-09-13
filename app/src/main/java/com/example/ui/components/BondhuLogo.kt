package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuTheme

enum class LogoSize(
    val fontSize: TextUnit,
    val dotSize: Dp,
    val spacing: Dp
) {
    MINI(12.sp, 3.dp, 2.dp),
    SMALL(16.sp, 4.dp, 3.dp),
    MEDIUM(18.sp, 5.dp, 3.5.dp),
    LARGE(24.sp, 6.dp, 4.dp),
    HERO(28.sp, 7.5.dp, 5.dp),
    SPLASH(44.sp, 10.dp, 7.dp)
}

// Exact brand colors from official brand mark
val BrandForestGreen = Color(0xFF1E4D38)
val BrandTerracottaDot = Color(0xFFC45A38)

/**
 * Official Wordmark "বন্ধু":
 * Matches the official design asset:
 * "বন্ধু" in deep forest green #1E4D38 (or dynamic emerald on dark),
 * with optional terracotta dot #C45A38 and matching "AI".
 * By default renders only "বন্ধু" ("the bo") as explicitly requested.
 */
@Composable
fun BondhuLogo(
    modifier: Modifier = Modifier,
    size: LogoSize = LogoSize.MEDIUM,
    showAi: Boolean = false,
    textColor: Color? = null,
    dotColor: Color = BrandTerracottaDot,
    aiColor: Color? = null
) {
    val colors = BondhuTheme.colors
    val effectiveTextColor = textColor ?: if (colors.isDark) colors.primary else BrandForestGreen
    val effectiveAiColor = aiColor ?: effectiveTextColor

    Row(
        modifier = modifier.testTag("bondhu_logo"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // "বন্ধু"
        Text(
            text = "বন্ধু",
            fontFamily = BalooDa2Family,
            fontWeight = FontWeight.Bold,
            fontSize = size.fontSize,
            color = effectiveTextColor,
            lineHeight = size.fontSize
        )

        if (showAi) {
            Spacer(modifier = Modifier.width(size.spacing))

            // Terracotta dot #C45A38
            Canvas(
                modifier = Modifier.size(size.dotSize)
            ) {
                drawCircle(
                    color = dotColor,
                    radius = this.size.minDimension / 2f
                )
            }

            Spacer(modifier = Modifier.width(size.spacing))

            // "AI" in matching brand green
            Text(
                text = "AI",
                fontFamily = BalooDa2Family,
                fontWeight = FontWeight.Bold,
                fontSize = size.fontSize,
                color = effectiveAiColor,
                lineHeight = size.fontSize
            )
        }
    }
}

