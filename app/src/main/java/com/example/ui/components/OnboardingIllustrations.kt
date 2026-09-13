package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BondhuTheme

/**
 * ═════════════════════════════════════════════════════════════════════
 * Fully Drawn Futuristic Abstract Humanoid Character Illustrations.
 * Sleek, complete silhouette with head, visor, neck, articulated torso,
 * arms, gestures, and legs with dynamic grounding.
 * Adapts dynamically to light and dark theme using BondhuTheme.colors.
 * ═════════════════════════════════════════════════════════════════════
 */

@Composable
fun OnboardingCharacterStep1Language(
    modifier: Modifier = Modifier,
    primaryColor: Color = BondhuTheme.colors.primary,
    accentColor: Color = BondhuTheme.colors.surfaceElevated,
    neutralColor: Color = BondhuTheme.colors.textSecondary
) {
    Canvas(modifier = modifier.size(width = 120.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height

        // Grounding subtle ambient ellipse shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.48f, h * 0.94f),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.16f, h * 0.90f),
            size = Size(w * 0.64f, h * 0.08f)
        )

        // Draw Base Full Human Body (Standing with confident gesture)
        drawFullHumanBody(
            cx = w * 0.42f,
            cy = h * 0.52f,
            scale = 0.88f,
            primaryColor = primaryColor,
            accentColor = accentColor,
            neutralColor = neutralColor,
            isRightArmRaised = true
        )

        // Language & Voice floating speech wave orb
        val orbCenter = Offset(w * 0.78f, h * 0.32f)
        drawCircle(
            color = primaryColor.copy(alpha = 0.18f),
            radius = w * 0.15f,
            center = orbCenter
        )
        drawCircle(
            color = primaryColor,
            radius = w * 0.11f,
            center = orbCenter,
            style = Stroke(width = 2.5f)
        )

        // Waveforms inside speech bubble
        val barYs = listOf(-6f, 0f, 6f)
        val barHeights = listOf(14f, 22f, 12f)
        barYs.forEachIndexed { index, xOff ->
            drawLine(
                color = primaryColor,
                start = Offset(orbCenter.x + xOff, orbCenter.y - barHeights[index] / 2f),
                end = Offset(orbCenter.x + xOff, orbCenter.y + barHeights[index] / 2f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        // Connection wave line from avatar to speech orb
        val connectPath = Path().apply {
            moveTo(w * 0.56f, h * 0.38f)
            quadraticBezierTo(w * 0.65f, h * 0.32f, w * 0.68f, h * 0.33f)
        }
        drawPath(
            path = connectPath,
            color = primaryColor.copy(alpha = 0.6f),
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun OnboardingCharacterStep2Name(
    modifier: Modifier = Modifier,
    primaryColor: Color = BondhuTheme.colors.primary,
    accentColor: Color = BondhuTheme.colors.surfaceElevated,
    neutralColor: Color = BondhuTheme.colors.textSecondary
) {
    Canvas(modifier = modifier.size(width = 120.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height

        // Grounding subtle ambient ellipse shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.45f, h * 0.94f),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.14f, h * 0.90f),
            size = Size(w * 0.62f, h * 0.08f)
        )

        // Draw Full Human Body
        drawFullHumanBody(
            cx = w * 0.40f,
            cy = h * 0.52f,
            scale = 0.88f,
            primaryColor = primaryColor,
            accentColor = accentColor,
            neutralColor = neutralColor,
            isRightArmRaised = true
        )

        // Futuristic Holographic ID Card held by right hand
        val cardTopLeft = Offset(w * 0.64f, h * 0.20f)
        val cardSize = Size(w * 0.30f, h * 0.22f)

        // Card glow & background
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.18f),
            topLeft = cardTopLeft,
            size = cardSize,
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = primaryColor,
            topLeft = cardTopLeft,
            size = cardSize,
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 2f)
        )

        // ID photo badge on card
        drawCircle(
            color = primaryColor,
            radius = w * 0.04f,
            center = Offset(cardTopLeft.x + w * 0.07f, cardTopLeft.y + h * 0.08f)
        )

        // ID text lines on card
        drawLine(
            color = primaryColor,
            start = Offset(cardTopLeft.x + w * 0.14f, cardTopLeft.y + h * 0.06f),
            end = Offset(cardTopLeft.x + cardSize.width - 8f, cardTopLeft.y + h * 0.06f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = neutralColor,
            start = Offset(cardTopLeft.x + w * 0.14f, cardTopLeft.y + h * 0.10f),
            end = Offset(cardTopLeft.x + cardSize.width - 12f, cardTopLeft.y + h * 0.10f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = neutralColor.copy(alpha = 0.6f),
            start = Offset(cardTopLeft.x + 8f, cardTopLeft.y + h * 0.16f),
            end = Offset(cardTopLeft.x + cardSize.width - 8f, cardTopLeft.y + h * 0.16f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun OnboardingCharacterStep3SignIn(
    modifier: Modifier = Modifier,
    primaryColor: Color = BondhuTheme.colors.primary,
    accentColor: Color = BondhuTheme.colors.surfaceElevated,
    neutralColor: Color = BondhuTheme.colors.textSecondary
) {
    Canvas(modifier = modifier.size(width = 120.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height

        // Grounding shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.50f, h * 0.94f),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.18f, h * 0.90f),
            size = Size(w * 0.64f, h * 0.08f)
        )

        // Full Human Body (Centered, confident posture)
        drawFullHumanBody(
            cx = w * 0.50f,
            cy = h * 0.54f,
            scale = 0.85f,
            primaryColor = primaryColor,
            accentColor = accentColor,
            neutralColor = neutralColor,
            isRightArmRaised = false
        )

        // Floating Shield & Lock above/around head
        val shieldCenter = Offset(w * 0.50f, h * 0.16f)
        val shieldPath = Path().apply {
            moveTo(shieldCenter.x, shieldCenter.y - 14f)
            lineTo(shieldCenter.x + 16f, shieldCenter.y - 8f)
            lineTo(shieldCenter.x + 14f, shieldCenter.y + 10f)
            quadraticBezierTo(shieldCenter.x, shieldCenter.y + 22f, shieldCenter.x, shieldCenter.y + 22f)
            quadraticBezierTo(shieldCenter.x, shieldCenter.y + 22f, shieldCenter.x - 14f, shieldCenter.y + 10f)
            lineTo(shieldCenter.x - 16f, shieldCenter.y - 8f)
            close()
        }

        drawPath(
            path = shieldPath,
            color = primaryColor.copy(alpha = 0.20f)
        )
        drawPath(
            path = shieldPath,
            color = primaryColor,
            style = Stroke(width = 2.5f, join = StrokeJoin.Round)
        )

        // Keyhole or check inside shield
        drawCircle(
            color = primaryColor,
            radius = 3.5f,
            center = Offset(shieldCenter.x, shieldCenter.y + 2f)
        )
        drawLine(
            color = primaryColor,
            start = Offset(shieldCenter.x, shieldCenter.y + 3f),
            end = Offset(shieldCenter.x, shieldCenter.y + 9f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun OnboardingCharacterStep4Terms(
    modifier: Modifier = Modifier,
    primaryColor: Color = BondhuTheme.colors.primary,
    accentColor: Color = BondhuTheme.colors.surfaceElevated,
    neutralColor: Color = BondhuTheme.colors.textSecondary
) {
    Canvas(modifier = modifier.size(width = 120.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height

        // Grounding shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.44f, h * 0.94f),
                radius = w * 0.38f
            ),
            topLeft = Offset(w * 0.12f, h * 0.90f),
            size = Size(w * 0.64f, h * 0.08f)
        )

        // Full Human Body
        drawFullHumanBody(
            cx = w * 0.38f,
            cy = h * 0.52f,
            scale = 0.88f,
            primaryColor = primaryColor,
            accentColor = accentColor,
            neutralColor = neutralColor,
            isRightArmRaised = true
        )

        // Floating Verified Document / Terms Sheet
        val docTopLeft = Offset(w * 0.62f, h * 0.16f)
        val docSize = Size(w * 0.32f, h * 0.34f)

        drawRoundRect(
            color = accentColor,
            topLeft = docTopLeft,
            size = docSize,
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = primaryColor.copy(alpha = 0.4f),
            topLeft = docTopLeft,
            size = docSize,
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 1.5f)
        )

        // Text lines on sheet
        val lineSpacing = 8f
        for (i in 0..2) {
            val y = docTopLeft.y + 14f + (i * lineSpacing)
            val endX = docTopLeft.x + docSize.width - if (i == 2) 20f else 10f
            drawLine(
                color = neutralColor.copy(alpha = 0.7f),
                start = Offset(docTopLeft.x + 8f, y),
                end = Offset(endX, y),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
        }

        // Glowing Approved Green Check Badge on document
        val badgeCenter = Offset(docTopLeft.x + docSize.width * 0.5f, docTopLeft.y + docSize.height * 0.74f)
        drawCircle(
            color = primaryColor,
            radius = w * 0.07f,
            center = badgeCenter
        )

        // Checkmark tick
        val checkPath = Path().apply {
            moveTo(badgeCenter.x - 5f, badgeCenter.y)
            lineTo(badgeCenter.x - 1f, badgeCenter.y + 4f)
            lineTo(badgeCenter.x + 6f, badgeCenter.y - 4f)
        }
        drawPath(
            path = checkPath,
            color = Color.White,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/**
 * Renders a complete, beautifully proportioned humanoid avatar:
 * - Rounded Head with futuristic visor / neon hairline accent
 * - Distinct neck connecting to shoulders
 * - Curved, sculpted torso with high-contrast chest core
 * - Left and right articulated arms with hands
 * - Fully drawn legs with knees and stylish sneakers
 */
private fun DrawScope.drawFullHumanBody(
    cx: Float,
    cy: Float,
    scale: Float,
    primaryColor: Color,
    accentColor: Color,
    neutralColor: Color,
    isRightArmRaised: Boolean
) {
    val unit = 40f * scale

    // 1. Head (Complete circle with sleek visor)
    val headRadius = unit * 0.65f
    val headCenter = Offset(cx, cy - unit * 2.2f)

    // Head base
    drawCircle(
        color = accentColor,
        radius = headRadius,
        center = headCenter
    )
    drawCircle(
        color = primaryColor.copy(alpha = 0.5f),
        radius = headRadius,
        center = headCenter,
        style = Stroke(width = 1.5f)
    )

    // Futuristic Neon Visor / Eye accent (curved glowing pill across face)
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(headCenter.x - headRadius * 0.7f, headCenter.y - headRadius * 0.15f),
        size = Size(headRadius * 1.4f, headRadius * 0.35f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // 2. Neck
    val neckTop = headCenter.y + headRadius * 0.65f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(cx - unit * 0.16f, neckTop),
        size = Size(unit * 0.32f, unit * 0.40f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // 3. Torso (Well proportioned, rounded shoulders and waist)
    val torsoTop = neckTop + unit * 0.28f
    val torsoWidth = unit * 1.45f
    val torsoHeight = unit * 1.60f
    val torsoRect = Rect(cx - torsoWidth / 2f, torsoTop, cx + torsoWidth / 2f, torsoTop + torsoHeight)

    drawRoundRect(
        color = accentColor,
        topLeft = Offset(torsoRect.left, torsoRect.top),
        size = Size(torsoWidth, torsoHeight),
        cornerRadius = CornerRadius(14f, 14f)
    )
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.35f),
        topLeft = Offset(torsoRect.left, torsoRect.top),
        size = Size(torsoWidth, torsoHeight),
        cornerRadius = CornerRadius(14f, 14f),
        style = Stroke(width = 1.5f)
    )

    // Center futuristic core badge on chest
    drawCircle(
        color = primaryColor,
        radius = unit * 0.18f,
        center = Offset(cx, torsoTop + unit * 0.50f)
    )

    // 4. Left Arm (Drawn hanging naturally with bent elbow)
    val leftArmPath = Path().apply {
        moveTo(torsoRect.left + 4f, torsoTop + unit * 0.20f)
        lineTo(torsoRect.left - unit * 0.40f, torsoTop + unit * 0.90f)
        lineTo(torsoRect.left - unit * 0.28f, torsoTop + unit * 1.55f)
    }
    drawPath(
        path = leftArmPath,
        color = accentColor,
        style = Stroke(width = unit * 0.32f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    drawPath(
        path = leftArmPath,
        color = primaryColor.copy(alpha = 0.4f),
        style = Stroke(width = unit * 0.32f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    // Left hand
    drawCircle(
        color = primaryColor,
        radius = unit * 0.16f,
        center = Offset(torsoRect.left - unit * 0.28f, torsoTop + unit * 1.58f)
    )

    // 5. Right Arm
    if (isRightArmRaised) {
        // Raised pointing or gesturing toward the prop
        val rightArmPath = Path().apply {
            moveTo(torsoRect.right - 4f, torsoTop + unit * 0.20f)
            lineTo(torsoRect.right + unit * 0.45f, torsoTop + unit * 0.40f)
            lineTo(torsoRect.right + unit * 0.75f, torsoTop - unit * 0.20f)
        }
        drawPath(
            path = rightArmPath,
            color = accentColor,
            style = Stroke(width = unit * 0.32f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Right hand
        drawCircle(
            color = primaryColor,
            radius = unit * 0.16f,
            center = Offset(torsoRect.right + unit * 0.75f, torsoTop - unit * 0.20f)
        )
    } else {
        // Hanging relaxed
        val rightArmPath = Path().apply {
            moveTo(torsoRect.right - 4f, torsoTop + unit * 0.20f)
            lineTo(torsoRect.right + unit * 0.40f, torsoTop + unit * 0.90f)
            lineTo(torsoRect.right + unit * 0.28f, torsoTop + unit * 1.55f)
        }
        drawPath(
            path = rightArmPath,
            color = accentColor,
            style = Stroke(width = unit * 0.32f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        drawCircle(
            color = primaryColor,
            radius = unit * 0.16f,
            center = Offset(torsoRect.right + unit * 0.28f, torsoTop + unit * 1.58f)
        )
    }

    // 6. Waist & Complete Legs
    val legWidth = unit * 0.40f
    val legHeight = unit * 1.70f
    val legY = torsoRect.bottom - 4f

    // Left Leg
    val leftLegX = cx - unit * 0.50f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(leftLegX, legY),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.3f),
        topLeft = Offset(leftLegX, legY),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    // Left Foot / Sleek Sneaker
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(leftLegX - unit * 0.12f, legY + legHeight - unit * 0.26f),
        size = Size(legWidth + unit * 0.20f, unit * 0.26f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Right Leg
    val rightLegX = cx + unit * 0.10f
    drawRoundRect(
        color = accentColor,
        topLeft = Offset(rightLegX, legY),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.3f),
        topLeft = Offset(rightLegX, legY),
        size = Size(legWidth, legHeight),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5f)
    )

    // Right Foot / Sleek Sneaker
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(rightLegX - unit * 0.04f, legY + legHeight - unit * 0.26f),
        size = Size(legWidth + unit * 0.20f, unit * 0.26f),
        cornerRadius = CornerRadius(6f, 6f)
    )
}
