package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldAccent
import kotlin.math.cos
import kotlin.math.sin

/**
 * Bondhu AI Sun Mark (Gemini's sparkle equivalent)
 * A radiant gold circle (#E8A83C) with 8 subtle rays.
 * - 20dp next to AI messages (like Gemini's sparkle)
 * - 48dp in empty chat state
 * - NEVER a generic robot
 */
@Composable
fun SunMark(
    size: Dp = 20.dp,
    color: Color = GoldAccent,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val center = Offset(w / 2f, h / 2f)

        // Center disk radius: ~28% of total width
        val diskRadius = w * 0.28f
        drawCircle(
            color = color,
            radius = diskRadius,
            center = center
        )

        // 8 subtle rays extending outwards
        val rayStart = diskRadius + (w * 0.07f)
        val rayEnd = w * 0.48f
        val strokeWidth = (w * 0.07f).coerceAtLeast(1.5f)

        for (i in 0 until 8) {
            val angleRad = Math.toRadians((i * 45.0)).toFloat()
            val cosA = cos(angleRad)
            val sinA = sin(angleRad)

            val pStart = Offset(
                x = center.x + cosA * rayStart,
                y = center.y + sinA * rayStart
            )
            val pEnd = Offset(
                x = center.x + cosA * rayEnd,
                y = center.y + sinA * rayEnd
            )

            drawLine(
                color = color.copy(alpha = 0.85f),
                start = pStart,
                end = pEnd,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}
