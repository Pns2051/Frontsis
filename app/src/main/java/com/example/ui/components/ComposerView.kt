package com.example.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.TravelExplore
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.NotoSansBengaliFamily
import java.io.BufferedReader
import java.io.InputStreamReader

data class AttachedFileInfo(
    val name: String,
    val sizeText: String,
    val isImage: Boolean,
    val textContent: String?
)

/**
 * ── COMPOSER (bottom) ──
 * Safe area padding
 * Pill container adapting smoothly to light & dark modes.
 * Inside:
 *   [📎] 20dp attach icon
 *   [auto-growing input] transparent bg, "Type a message…"
 *   [⚡ model label]
 *   [➤] 40dp circle send button
 *   Streaming: [■] 40dp circle stop button
 */
@Composable
fun ComposerView(
    text: String,
    onTextChange: (String) -> Unit,
    isStreaming: Boolean,
    currentModel: String,
    language: String,
    isWebSearchEnabled: Boolean = false,
    onToggleWebSearch: () -> Unit = {},
    onSend: (message: String, attachment: AttachedFileInfo?) -> Unit,
    onStop: () -> Unit,
    onOpenModelSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BondhuTheme.colors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var attachedFile by remember { mutableStateOf<AttachedFileInfo?>(null) }

    // File Picker for attachments (text, pdf, images)
    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileInfo = parseUriFileInfo(context, uri)
            if (fileInfo != null) {
                attachedFile = fileInfo
            } else {
                Toast.makeText(context, Strings.fileSizeLimit(language), Toast.LENGTH_SHORT).show()
            }
        }
    }

    val pillBg = if (colors.isDark) Color(0xFF1F1F1F) else Color(0xFFFFFFFF)
    val barBg = if (colors.isDark) Color(0xFF141414) else Color(0xFFF9FAFB)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(barBg)
            .border(
                width = 1.dp,
                color = colors.border.copy(alpha = 0.6f)
            )
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // File bar (above composer)
        AnimatedVisibility(
            visible = attachedFile != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            attachedFile?.let { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pillBg)
                        .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isImage) Icons.Default.Image else Icons.Default.Description,
                            contentDescription = "Attachment",
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${file.name} (${file.sizeText})",
                            fontFamily = NotoSansBengaliFamily,
                            fontSize = 12.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { attachedFile = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Strings.removeAttachment(language),
                            tint = colors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Pill Composer: 100% Solid Opaque, high-contrast, perfectly rounded, no transparency issues
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(pillBg)
                .border(
                    width = 1.5.dp,
                    color = if (text.isNotBlank()) colors.primary.copy(alpha = 0.5f) else colors.border,
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // [📎] 20dp attach icon
                IconButton(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("attach_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = Strings.attachFile(language),
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // [auto-growing input] transparent bg, "Type a message…"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 8.dp)
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = Strings.composerPlaceholder(language),
                            fontFamily = NotoSansBengaliFamily,
                            fontSize = 15.sp,
                            color = colors.textSecondary
                        )
                    }
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 22.dp, max = 120.dp)
                            .testTag("chat_input_field"),
                        textStyle = TextStyle(
                            fontFamily = NotoSansBengaliFamily,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        ),
                        cursorBrush = SolidColor(colors.primary),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        )
                    )
                }

                // [🌐 Web Search toggle]
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isWebSearchEnabled) colors.primary.copy(alpha = 0.2f) else colors.surfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isWebSearchEnabled) colors.primary else colors.border,
                            shape = CircleShape
                        )
                        .clickable(onClick = onToggleWebSearch)
                        .testTag("web_search_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.TravelExplore,
                        contentDescription = "Web Search",
                        tint = if (isWebSearchEnabled) colors.primary else colors.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // [⚡ model label] 12sp, tappable to open model sheet
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .clickable(onClick = onOpenModelSelector)
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                        .testTag("model_selector_badge"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = "Model",
                        tint = colors.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (currentModel == "reasoning") "5.3" else "Light",
                        fontFamily = NotoSansBengaliFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        color = colors.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Action Button: [➤] Send or [■] Stop with smooth spring transition
                AnimatedContent(
                    targetState = isStreaming,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                                (scaleOut() + fadeOut())
                    },
                    label = "composer_action_button"
                ) { streaming ->
                    if (streaming) {
                        // Streaming: [■] 40dp circle, white square
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colors.error)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStop()
                                }
                                .testTag("stop_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = Strings.stop(language),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        val canSend = text.isNotBlank() || attachedFile != null
                        val buttonScale by animateFloatAsState(
                            targetValue = if (canSend) 1f else 0.94f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "send_button_scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(buttonScale)
                                .clip(CircleShape)
                                .background(if (canSend) colors.primary else colors.surfaceElevated)
                                .clickable(enabled = canSend) {
                                    if (canSend) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        val currentText = text.trim()
                                        val file = attachedFile
                                        attachedFile = null
                                        onSend(currentText, file)
                                    }
                                }
                                .testTag("send_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = Strings.send(language),
                                tint = if (canSend) colors.onPrimary else colors.textTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun parseUriFileInfo(context: Context, uri: Uri): AttachedFileInfo? {
    try {
        var name = "attachment"
        var sizeBytes = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex != -1) name = cursor.getString(nameIndex)
                if (sizeIndex != -1) sizeBytes = cursor.getLong(sizeIndex)
            }
        }

        // Limit: 5MB
        if (sizeBytes > 5 * 1024 * 1024) return null

        val mimeType = context.contentResolver.getType(uri) ?: ""
        val isImage = mimeType.startsWith("image/")

        val sizeText = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0))
        }

        var textContent: String? = null
        if (!isImage) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream, Charsets.UTF_8))
                    val sb = StringBuilder()
                    var line = reader.readLine()
                    var lineCount = 0
                    while (line != null && lineCount < 200) {
                        sb.appendLine(line)
                        line = reader.readLine()
                        lineCount++
                    }
                    textContent = sb.toString()
                }
            } catch (_: Exception) {}
        }

        return AttachedFileInfo(
            name = name,
            sizeText = sizeText,
            isImage = isImage,
            textContent = textContent
        )
    } catch (_: Exception) {
        return null
    }
}
