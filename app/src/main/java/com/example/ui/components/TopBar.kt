package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * ── TOP BAR (56dp) ──
 * Modern, sleek, responsive to Light & Dark theme.
 * Left: [☰] menu icon
 * Center: "বন্ধু·AI" logo with emerald AI badge
 * Right: [⚡ 42] lightning credits pill + [＋] new chat button
 */
@Composable
fun TopBar(
    credits: Int,
    language: String,
    onMenuClick: () -> Unit,
    onNewChatClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val haptic = LocalHapticFeedback.current
    var showTooltip by remember { mutableStateOf(false) }
    val isLowCredits = credits <= 5
    val pillBg = if (colors.isDark) Color(0xFF1E1E1E) else Color(0xFFF3F4F6)
    val boltColor = if (isLowCredits) colors.error else Color(0xFFF59E0B)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: [☰] menu icon
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onMenuClick()
                },
                modifier = Modifier
                    .size(44.dp)
                    .testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Menu,
                    contentDescription = "Menu",
                    tint = colors.textPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Center: "বন্ধু · AI" - Prominent, crisp, unclipped wordmark
            BondhuLogo(
                size = LogoSize.LARGE,
                showAi = true
            )

            // Right Items: [⚡ 42] and [＋]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                // Credits Pill: Ultra-sleek, minimalist capsule [⚡ 42]
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(pillBg)
                            .border(
                                width = 1.dp,
                                color = if (isLowCredits) colors.error.copy(alpha = 0.5f) else colors.border,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { showTooltip = !showTooltip }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("credits_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = "Credits",
                                tint = boltColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$credits",
                                fontFamily = BalooDa2Family,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isLowCredits) colors.error else colors.textPrimary
                            )
                        }
                    }

                    // Tooltip on tap
                    DropdownMenu(
                        expanded = showTooltip,
                        onDismissRequest = { showTooltip = false },
                        modifier = Modifier.background(colors.surfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = Strings.creditsTooltip(language),
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                            },
                            onClick = { showTooltip = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // [＋] new chat
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onNewChatClick()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("new_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = Strings.newChat(language),
                        tint = colors.textPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

