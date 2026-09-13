package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.HindSiliguriFamily

/**
 * ── MODEL SHEET (bottom sheet) ──
 * Modern M3 modal bottom sheet with full BondhuTheme light & dark mode support.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelBottomSheet(
    selectedModel: String,
    language: String,
    onModelSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = BondhuTheme.colors
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
        containerColor = colors.surface,
        contentColor = colors.textPrimary,
        modifier = modifier.testTag("model_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            // "Select AI Model"
            Text(
                text = Strings.selectModel(language),
                fontFamily = BalooDa2Family,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 1. "Bondhu Light" [Default green badge]
            val isLightSelected = selectedModel == "light"
            ModelOptionCard(
                title = Strings.modelLight(language),
                badgeText = Strings.modelLightBadge(language),
                badgeBg = colors.primary.copy(alpha = 0.16f),
                badgeColor = colors.primary,
                description = Strings.modelLightDesc(language),
                isSelected = isLightSelected,
                icon = Icons.Default.Speed,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModelSelect("light")
                    onDismiss()
                },
                testTag = "model_option_light"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. "Bondhu 5.3 Reasoning" [Advanced red badge]
            val isReasoningSelected = selectedModel == "reasoning"
            ModelOptionCard(
                title = Strings.modelReasoning(language),
                badgeText = Strings.modelReasoningBadge(language),
                badgeBg = colors.error.copy(alpha = 0.16f),
                badgeColor = colors.error,
                description = Strings.modelReasoningDesc(language),
                isSelected = isReasoningSelected,
                icon = Icons.Default.Psychology,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onModelSelect("reasoning")
                    onDismiss()
                },
                testTag = "model_option_reasoning"
            )
        }
    }
}

@Composable
private fun ModelOptionCard(
    title: String,
    badgeText: String,
    badgeBg: Color,
    badgeColor: Color,
    description: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val colors = BondhuTheme.colors
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.primary else colors.border,
        animationSpec = tween(220),
        label = "model_card_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) colors.surfaceRaised else colors.surface,
        animationSpec = tween(220),
        label = "model_card_bg"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.primary else colors.textSecondary,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontFamily = BalooDa2Family,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontFamily = HindSiliguriFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    fontFamily = HindSiliguriFamily,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
