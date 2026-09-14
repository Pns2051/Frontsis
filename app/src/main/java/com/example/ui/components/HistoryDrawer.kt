package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatSession
import com.example.ui.i18n.Strings
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.NotoSansBengaliFamily

/**
 * ═══════════ SCREEN 7: HISTORY DRAWER ═══════════
 * Sleek, modern, futuristic sidebar with:
 * - Dynamic light/dark theme support via BondhuTheme.colors
 * - Quick Topics & Section prompts relocated into the sidebar
 * - Chat history list with active indicator & delete actions
 * - User account footer
 */
@Composable
fun HistoryDrawerContent(
    sessions: List<ChatSession>,
    currentSessionId: String?,
    language: String,
    userName: String = "",
    userEmail: String = "",
    loginType: String = "guest",
    onNewChatClick: () -> Unit,
    onSessionClick: (ChatSession) -> Unit,
    onDeleteSessionClick: (ChatSession) -> Unit,
    onSectionPromptClick: (String) -> Unit = {},
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
    val colors = BondhuTheme.colors
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(290.dp)
            .background(colors.surface)
            .statusBarsPadding()
            .padding(16.dp)
            .testTag("history_drawer")
    ) {
        // Header: Logo (only "bo" as requested)
        BondhuLogo(
            size = LogoSize.MEDIUM,
            showAi = false,
            modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)
        )

        // [New Chat] Button
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onNewChatClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("drawer_new_chat_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = Strings.newChat(language),
                fontFamily = NotoSansBengaliFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Topic Sections in Sidebar (Relocated as requested to declutter center)
        Text(
            text = if (language == "bn") "বিষয় ও প্রম্পট" else "Topics & Prompts",
            fontFamily = NotoSansBengaliFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        val promptChips = Strings.suggestions(language)
        val promptIcons = listOf(
            Icons.Outlined.Translate,
            Icons.Outlined.Code,
            Icons.Outlined.Lightbulb,
            Icons.AutoMirrored.Outlined.Chat
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            promptChips.take(4).forEachIndexed { index, chipText ->
                val icon = promptIcons.getOrElse(index) { Icons.Outlined.Lightbulb }
                SidebarPromptItem(
                    icon = icon,
                    label = chipText,
                    onClick = {
                        onSectionPromptClick(Strings.suggestionPrompt(chipText, language))
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = colors.border, thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // Label: "History"
        Text(
            text = Strings.history(language),
            fontFamily = NotoSansBengaliFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.textSecondary
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List or Empty State
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Strings.noHistory(language),
                    fontFamily = NotoSansBengaliFamily,
                    fontSize = 13.5.sp,
                    color = colors.textTertiary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(sessions, key = { it.id }) { session ->
                    val isActive = session.id == currentSessionId
                    Box(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) colors.surfaceRaised else Color.Transparent)
                            .then(
                                if (isActive) {
                                    Modifier.border(
                                        width = 1.dp,
                                        color = colors.primary.copy(alpha = 0.45f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                } else Modifier
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSessionClick(session)
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("session_item_${session.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Left accent line if active
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors.primary)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.title.ifBlank { Strings.newChat(language) },
                                    fontFamily = NotoSansBengaliFamily,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isActive) colors.primary else colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                val msgCount = session.messageCount.coerceAtLeast(1)
                                val timeText = session.createdAt?.take(10) ?: "recent"
                                Text(
                                    text = "$msgCount msgs · $timeText",
                                    fontFamily = NotoSansBengaliFamily,
                                    fontSize = 11.sp,
                                    color = colors.textTertiary
                                )
                            }

                            // [✕] delete button
                            IconButton(
                                onClick = { sessionToDelete = session },
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_session_${session.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = Strings.delete(language),
                                    tint = colors.error.copy(alpha = 0.85f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = colors.border, thickness = 1.dp)

        Spacer(modifier = Modifier.height(10.dp))

        // Footer: Avatar, name, email → tap opens Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onSettingsClick)
                .padding(6.dp)
                .testTag("drawer_user_footer"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                val initial = (userName.trim().firstOrNull() ?: 'B').uppercaseChar()
                Text(
                    text = "$initial",
                    fontFamily = NotoSansBengaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName.ifBlank { "User" },
                    fontFamily = NotoSansBengaliFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (loginType == "google" && userEmail.isNotBlank()) userEmail else Strings.guest(language),
                    fontFamily = NotoSansBengaliFamily,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = {
                Text(
                    text = Strings.deleteChatTitle(language),
                    fontFamily = NotoSansBengaliFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = Strings.deleteChatMsg(language),
                    fontFamily = NotoSansBengaliFamily,
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteSessionClick(session)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.error,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Strings.delete(language),
                        fontFamily = NotoSansBengaliFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { sessionToDelete = null },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = Strings.cancel(language),
                        fontFamily = NotoSansBengaliFamily,
                        color = colors.textSecondary
                    )
                }
            },
            containerColor = colors.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun SidebarPromptItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val colors = BondhuTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceRaised)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontFamily = NotoSansBengaliFamily,
            fontSize = 12.5.sp,
            color = colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
