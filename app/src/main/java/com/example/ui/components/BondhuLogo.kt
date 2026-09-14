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
import androidx.compose.ui.text.style.TextAlign
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.NotoSansBengaliFamily

enum class LogoSize(
    val fontSize: TextUnit,
    val dotSize: Dp,
    val spacing: Dp
) {
    MINI(13.sp, 3.dp, 2.dp),
    SMALL(16.sp, 4.dp, 3.dp),
    MEDIUM(21.sp, 5.dp, 4.dp),
    LARGE(25.sp, 6.dp, 4.5.dp),
    HERO(30.sp, 7.5.dp, 5.dp),
    SPLASH(46.sp, 10.dp, 7.dp)
}

// Exact brand colors from official brand mark
val BrandForestGreen = Color(0xFF1E4D38)
val BrandTerracottaDot = Color(0xFFC45A38)

/**
 * Official Wordmark "বন্ধু":
 * Uses Google's authentic Noto Sans Bengali font for crisp, unclipped Bengali ligatures
 * with full ascender (matra) and descender (u-kar) breathing room.
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
        // "বন্ধু" - Sturdy, unclipped, robust Bengali typography
        Text(
            text = "বন্ধু",
            fontFamily = NotoSansBengaliFamily,
            fontWeight = FontWeight.Bold,
            fontSize = size.fontSize,
            color = effectiveTextColor,
            lineHeight = (size.fontSize.value * 1.35f).sp,
            textAlign = TextAlign.Center
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
                fontFamily = NotoSansBengaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = size.fontSize,
                color = effectiveAiColor,
                lineHeight = (size.fontSize.value * 1.35f).sp
            )
        }
    }
}

