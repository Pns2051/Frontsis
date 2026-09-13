package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.i18n.Strings
import com.example.ui.theme.BalooDa2Family
import com.example.ui.theme.BondhuColorPalette
import com.example.ui.theme.BondhuTheme
import com.example.ui.theme.HindSiliguriFamily

/**
 * ── MESSAGES ──
 * User: #22C55E bg, #FFFFFF text, right-aligned
 *   Radius: 16dp top & bottom-left, 4dp bottom-right
 * AI: NO bg — just #FFFFFF text, sun mark 20dp on left, 15sp
 * Markdown (AI): bold, lists, `code`, code blocks + copy button
 * Copy button: [⧉ Copy] 12sp #8E8E8E, toast "Copied"
 */

private data class ParsedThought(
    val thoughtContent: String?,
    val isThinkingOpen: Boolean,
    val mainContent: String
)

private fun extractThoughtContent(rawText: String): ParsedThought {
    val thinkOpenTag = "<think>"
    val thinkCloseTag = "</think>"
    val altThinkOpenTag = "<thought>"
    val altThinkCloseTag = "</thought>"

    val openTag: String
    val closeTag: String

    if (rawText.contains(thinkOpenTag)) {
        openTag = thinkOpenTag
        closeTag = thinkCloseTag
    } else if (rawText.contains(altThinkOpenTag)) {
        openTag = altThinkOpenTag
        closeTag = altThinkCloseTag
    } else {
        return ParsedThought(null, false, rawText)
    }

    val startIndex = rawText.indexOf(openTag)
    val contentAfterOpen = rawText.substring(startIndex + openTag.length)
    val closeIndex = contentAfterOpen.indexOf(closeTag)

    return if (closeIndex != -1) {
        val thought = contentAfterOpen.substring(0, closeIndex).trim()
        val remainder = rawText.substring(0, startIndex) + contentAfterOpen.substring(closeIndex + closeTag.length)
        ParsedThought(thought, false, remainder.trim())
    } else {
        ParsedThought(contentAfterOpen.trim(), true, rawText.substring(0, startIndex).trim())
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    language: String,
    isColdStart: Boolean = false,
    onRetry: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    val colors = BondhuTheme.colors

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(180, easing = FastOutSlowInEasing)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (isUser) {
                // User: Primary bubble, onPrimary text, right-aligned, Radius: 16dp (bottom-right 4dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 4.dp
                            )
                        )
                        .background(colors.primary)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .testTag("user_message_bubble"),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        if (message.attachmentName != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(bottom = 6.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(colors.onPrimary.copy(alpha = 0.18f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AttachFile,
                                    contentDescription = null,
                                    tint = colors.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = message.attachmentName,
                                    fontFamily = HindSiliguriFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.onPrimary
                                )
                            }
                        }

                        Text(
                            text = message.content,
                            fontFamily = HindSiliguriFamily,
                            color = colors.onPrimary,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            } else {
                // AI: Modern layout, AI badge, crisp text adapting to light/dark
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_message_row"),
                    verticalAlignment = Alignment.Top
                ) {
                    // Modern AI Avatar badge (replacing sun icon)
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp, end = 12.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
                            .border(
                                width = 1.dp,
                                color = colors.primary.copy(alpha = if (colors.isDark) 0.45f else 0.35f),
                                shape = CircleShape
                            )
                            .testTag("ai_avatar_badge"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoAwesome,
                            contentDescription = "AI",
                            tint = colors.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (message.isError) {
                            // Error notification box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, colors.error.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .background(colors.surface)
                                    .padding(12.dp)
                                    .testTag("error_message_bubble")
                            ) {
                                Column {
                                    Text(
                                        text = message.errorMessage ?: message.content,
                                        fontFamily = HindSiliguriFamily,
                                        color = colors.error,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 19.sp
                                    )

                                    if (message.canRetry && !message.retryPrompt.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { onRetry(message.retryPrompt) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = colors.error,
                                                contentColor = Color.White
                                            ),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = Strings.retry(language),
                                                fontFamily = BalooDa2Family,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            val parsed = extractThoughtContent(message.content)
                            var thoughtExpanded by remember { mutableStateOf(parsed.isThinkingOpen) }

                            // Thought disclosure card for reasoning models
                            if (!parsed.thoughtContent.isNullOrBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(colors.surface)
                                        .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                        .clickable { thoughtExpanded = !thoughtExpanded }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (parsed.isThinkingOpen) {
                                                Icon(
                                                    imageVector = Icons.Outlined.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = colors.primary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = if (parsed.isThinkingOpen) Strings.thinking(language) else Strings.thinkingDone(language),
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 12.sp,
                                                color = if (parsed.isThinkingOpen) colors.primary else colors.textSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = if (thoughtExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                                            contentDescription = null,
                                            tint = colors.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (thoughtExpanded) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surfaceElevated)
                                            .padding(10.dp)
                                    ) {
                                        Text(
                                            text = parsed.thoughtContent,
                                            fontFamily = HindSiliguriFamily,
                                            fontSize = 12.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 17.sp
                                        )
                                    }
                                }
                            }

                            // AI Main Content (Markdown parsed: bold, lists, code, code blocks)
                            val displayContent = if (parsed.thoughtContent != null) parsed.mainContent else message.content
                            if (message.isStreaming && displayContent.isBlank()) {
                                InlineThinkingCard(
                                    isColdStart = isColdStart,
                                    language = language,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            } else {
                                MarkdownTextRenderer(
                                    content = displayContent,
                                    colors = colors,
                                    onCopyCode = { code ->
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Bondhu AI", code))
                                        Toast.makeText(context, Strings.copied(language), Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }

                            // AI Message Bottom Actions: [⧉ Copy] (only when message has completed)
                            if (!message.isStreaming && displayContent.isNotBlank()) {
                                Row(
                                    modifier = Modifier
                                        .padding(top = 10.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colors.surfaceElevated)
                                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Bondhu AI", displayContent))
                                                Toast.makeText(context, Strings.copied(language), Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = Strings.copy(language),
                                                tint = colors.textSecondary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = Strings.copy(language),
                                                fontFamily = HindSiliguriFamily,
                                                fontSize = 11.5.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Parses markdown into clean Compose text, bold, inline code, lists, and code blocks
 */
@Composable
private fun MarkdownTextRenderer(
    content: String,
    colors: BondhuColorPalette,
    onCopyCode: (String) -> Unit
) {
    if (content.isBlank()) return

    val blocks = splitMarkdownBlocks(content)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    // Code Block with Copy button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceElevated)
                            .border(1.dp, colors.border, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = block.language.ifBlank { "code" },
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Row(
                                    modifier = Modifier.clickable { onCopyCode(block.code) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy code",
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy",
                                        fontFamily = HindSiliguriFamily,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                Text(
                                    text = block.code,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = formatInlineMarkdown(block.text, colors),
                        fontFamily = HindSiliguriFamily,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        lineHeight = 23.sp
                    )
                }

                is MarkdownBlock.ListItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = if (block.orderedNumber != null) "${block.orderedNumber}. " else "• ",
                            fontFamily = HindSiliguriFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = colors.primary
                        )
                        Text(
                            text = formatInlineMarkdown(block.text, colors),
                            fontFamily = HindSiliguriFamily,
                            fontSize = 15.sp,
                            color = colors.textPrimary,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class ListItem(val text: String, val orderedNumber: Int? = null) : MarkdownBlock()
}

private fun splitMarkdownBlocks(raw: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = raw.lines()
    var inCodeBlock = false
    var codeLang = ""
    val codeContent = StringBuilder()
    val paragraphContent = StringBuilder()

    fun flushParagraph() {
        if (paragraphContent.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(paragraphContent.toString().trim()))
            paragraphContent.setLength(0)
        }
    }

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("```")) {
            if (!inCodeBlock) {
                flushParagraph()
                inCodeBlock = true
                codeLang = trimmed.removePrefix("```").trim()
                codeContent.setLength(0)
            } else {
                inCodeBlock = false
                blocks.add(MarkdownBlock.CodeBlock(codeLang, codeContent.toString().trimEnd()))
                codeContent.setLength(0)
            }
            continue
        }

        if (inCodeBlock) {
            codeContent.appendLine(line)
            continue
        }

        // Check for bullet list item
        if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
            flushParagraph()
            blocks.add(MarkdownBlock.ListItem(trimmed.substring(2).trim()))
            continue
        }

        // Check for numbered list item
        val numberedMatch = Regex("^(\\d+)\\.\\s+(.*)").find(trimmed)
        if (numberedMatch != null) {
            flushParagraph()
            val num = numberedMatch.groupValues[1].toIntOrNull() ?: 1
            val itemText = numberedMatch.groupValues[2]
            blocks.add(MarkdownBlock.ListItem(itemText, num))
            continue
        }

        if (trimmed.isEmpty()) {
            flushParagraph()
        } else {
            if (paragraphContent.isNotEmpty()) {
                paragraphContent.append(" ")
            }
            paragraphContent.append(line)
        }
    }

    if (inCodeBlock) {
        blocks.add(MarkdownBlock.CodeBlock(codeLang, codeContent.toString()))
    } else {
        flushParagraph()
    }

    return blocks
}

private fun formatInlineMarkdown(text: String, colors: BondhuColorPalette): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            // Bold: **...**
            if (i + 1 < text.length && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    val boldText = text.substring(i + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldText)
                    pop()
                    i = end + 2
                    continue
                }
            }

            // Inline Code: `...`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    val codeText = text.substring(i + 1, end)
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            color = colors.primary,
                            background = colors.surfaceElevated
                        )
                    )
                    append(" $codeText ")
                    pop()
                    i = end + 1
                    continue
                }
            }

            append(text[i])
            i++
        }
    }
}
