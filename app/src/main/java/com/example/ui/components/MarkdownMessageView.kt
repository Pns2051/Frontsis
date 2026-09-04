package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ChatMessage
import com.example.ui.i18n.Strings
import com.example.ui.theme.BengalGreen
import com.example.ui.theme.BengalGreenBright
import com.example.ui.theme.SunriseRed
import com.example.ui.theme.SunriseRedLight
import com.example.ui.theme.UserBubbleGradient

@Composable
fun MarkdownMessageView(
    message: ChatMessage,
    language: String,
    isSpeaking: Boolean = false,
    onToggleSpeak: () -> Unit = {},
    onShare: () -> Unit = {},
    onRegenerate: (() -> Unit)? = null,
    onRetry: (String) -> Unit,
    onCopyToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isUser = message.role == "user"
    val context = LocalContext.current
    var isMessageCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isMessageCopied) {
        if (isMessageCopied) {
            delay(1800)
            isMessageCopied = false
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn(tween(250, easing = FastOutSlowInEasing)) + slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) { 25 },
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            if (isUser) {
                // User Bubble: green gradient, white text, right-aligned, 16dp rounded corners
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                        .background(UserBubbleGradient)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .testTag("user_message_bubble")
                ) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                }
            } else {
                // AI Bubble: white/dark surface, soft border, left-aligned, 16dp corners
                Column(
                    modifier = Modifier.fillMaxWidth(0.92f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_app_logo),
                            contentDescription = "Bondhu Logo",
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Strings.appName(language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (message.isError) {
                        // Red-tinted Error Bubble
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, SunriseRed.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                .padding(14.dp)
                                .testTag("error_message_bubble")
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SunriseRed, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = message.errorMessage ?: message.content,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 20.sp
                                    )
                                }

                                if (message.canRetry && !message.retryPrompt.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { onRetry(message.retryPrompt) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SunriseRed,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("retry_send_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Strings.sendAgain(language),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Normal AI Bubble with Markdown parsing
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(horizontal = 16.dp, vertical = 13.dp)
                                .testTag("ai_message_bubble")
                        ) {
                            MarkdownContent(
                                content = message.content,
                                isStreaming = message.isStreaming,
                                onCopyCode = { code ->
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Bondhu AI Code", code))
                                    onCopyToast()
                                }
                            )
                        }

                        // Small Action Bar below AI bubble (Copy button with smooth animated checkmark)
                        if (!message.isStreaming && message.content.isNotBlank()) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 4.dp, start = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Bondhu AI", message.content))
                                        isMessageCopied = true
                                        onCopyToast()
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("copy_ai_message_button")
                                ) {
                                    AnimatedContent(
                                        targetState = isMessageCopied,
                                        transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                                        label = "copy_msg_icon_anim"
                                    ) { copied ->
                                        if (copied) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Copied",
                                                modifier = Modifier.size(15.dp),
                                                tint = BengalGreenBright
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = Strings.copy(language),
                                                modifier = Modifier.size(15.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Read Aloud (TTS) Button
                                IconButton(
                                    onClick = onToggleSpeak,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("speak_ai_message_button")
                                ) {
                                    Icon(
                                        imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                        contentDescription = if (isSpeaking) Strings.stopSpeaking(language) else Strings.speak(language),
                                        modifier = Modifier.size(15.dp),
                                        tint = if (isSpeaking) BengalGreenBright else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Share Button
                                IconButton(
                                    onClick = onShare,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("share_ai_message_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = Strings.share(language),
                                        modifier = Modifier.size(15.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Regenerate Button (if available)
                                if (onRegenerate != null) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = onRegenerate,
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("regenerate_ai_message_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = Strings.regenerate(language),
                                            modifier = Modifier.size(15.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

@Composable
private fun MarkdownContent(
    content: String,
    isStreaming: Boolean,
    onCopyCode: (String) -> Unit
) {
    // Blinking green caret during streaming
    val caretAlpha = remember { Animatable(1f) }
    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            caretAlpha.animateTo(
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(450, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            caretAlpha.snapTo(0f)
        }
    }

    if (content.isEmpty() && isStreaming) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 8.dp, height = 18.dp)
                    .alpha(caretAlpha.value)
                    .background(BengalGreenBright, RoundedCornerShape(2.dp))
            )
        }
        return
    }

    val blocks = remember(content) { parseMarkdownBlocks(content) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code,
                        onCopy = { onCopyCode(block.code) }
                    )
                }
                is MarkdownBlock.Header -> {
                    Text(
                        text = buildAnnotatedMarkdown(block.text),
                        fontSize = when (block.level) {
                            1 -> 20.sp
                            2 -> 18.sp
                            else -> 16.sp
                        },
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
                    )
                }
                is MarkdownBlock.ListItem -> {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (block.isOrdered) "${block.number}. " else "• ",
                            fontWeight = FontWeight.Bold,
                            color = BengalGreen,
                            fontSize = 15.sp
                        )
                        Text(
                            text = buildAnnotatedMarkdown(block.text),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
                is MarkdownBlock.Table -> {
                    TableView(rows = block.rows)
                }
                is MarkdownBlock.Paragraph -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = buildAnnotatedMarkdown(block.text),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }

        // Blinking caret at the end of streaming text
        if (isStreaming) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(width = 8.dp, height = 18.dp)
                    .alpha(caretAlpha.value)
                    .background(BengalGreenBright, RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    var isCodeCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCodeCopied) {
        if (isCodeCopied) {
            delay(1800)
            isCodeCopied = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2421))
            .border(1.dp, Color(0xFF2E3A33), RoundedCornerShape(8.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF161C19))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" },
                color = Color(0xFFA8B8B0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable {
                        isCodeCopied = true
                        onCopy()
                    }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = isCodeCopied,
                    transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                    label = "code_copy_anim"
                ) { copied ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (copied) BengalGreenBright else Color(0xFFA8B8B0),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (copied) "Copied!" else "Copy",
                            color = if (copied) BengalGreenBright else Color(0xFFA8B8B0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Code text with horizontal scroll
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            Text(
                text = code,
                color = Color(0xFFE2EFE9),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun TableView(rows: List<List<String>>) {
    if (rows.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
            .horizontalScroll(rememberScrollState())
    ) {
        rows.forEachIndexed { index, row ->
            val isHeader = index == 0
            Row(
                modifier = Modifier
                    .background(
                        if (isHeader) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        else if (index % 2 == 1) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 80.dp, max = 200.dp)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = cell.trim(),
                            fontSize = 13.sp,
                            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val text: String) : MarkdownBlock()
    data class Header(val text: String, val level: Int) : MarkdownBlock()
    data class ListItem(val text: String, val isOrdered: Boolean, val number: Int = 1) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class Table(val rows: List<List<String>>) : MarkdownBlock()
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.split("\n")
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block
        if (line.trim().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeBuilder = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeBuilder.append(lines[i]).append("\n")
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeBuilder.toString().trimEnd()))
            i++
            continue
        }

        // Table
        if (line.contains("|") && line.trim().startsWith("|") && line.trim().endsWith("|")) {
            val tableRows = mutableListOf<List<String>>()
            while (i < lines.size && lines[i].contains("|") && lines[i].trim().startsWith("|")) {
                val rowLine = lines[i].trim()
                // Skip separator row |---|---|
                if (!rowLine.matches(Regex("\\|[\\s\\-:]+\\|.*"))) {
                    val cells = rowLine.split("|").filterIndexed { idx, _ -> idx > 0 && idx < rowLine.split("|").size - 1 }
                    tableRows.add(cells)
                }
                i++
            }
            blocks.add(MarkdownBlock.Table(tableRows))
            continue
        }

        // Headers
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            val headerText = line.drop(level).trim()
            blocks.add(MarkdownBlock.Header(headerText, level))
            i++
            continue
        }

        // Unordered list
        if (line.trim().startsWith("- ") || line.trim().startsWith("* ")) {
            val itemText = line.trim().drop(2).trim()
            blocks.add(MarkdownBlock.ListItem(itemText, isOrdered = false))
            i++
            continue
        }

        // Ordered list
        val orderedMatch = Regex("^(\\d+)\\.\\s+(.*)").find(line.trim())
        if (orderedMatch != null) {
            val num = orderedMatch.groupValues[1].toIntOrNull() ?: 1
            val text = orderedMatch.groupValues[2]
            blocks.add(MarkdownBlock.ListItem(text, isOrdered = true, number = num))
            i++
            continue
        }

        // Regular paragraph
        if (line.isNotBlank()) {
            blocks.add(MarkdownBlock.Paragraph(line))
        }

        i++
    }

    return blocks
}

private fun buildAnnotatedMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val italicRegex = Regex("\\*(.*?)\\*")
        val codeRegex = Regex("`(.*?)`")

        // Simple tokenizer for bold, italic, and inline code
        val combinedRegex = Regex("(\\*\\*.*?\\*\\*)|(`.*?`)|(\\*.*?\\*)")
        val matches = combinedRegex.findAll(text)

        var lastIdx = 0
        for (match in matches) {
            if (match.range.first > lastIdx) {
                append(text.substring(lastIdx, match.range.first))
            }
            val matchText = match.value
            when {
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(matchText.removeSurrounding("**"))
                    pop()
                }
                matchText.startsWith("`") && matchText.endsWith("`") -> {
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x22006A4E)))
                    append(matchText.removeSurrounding("`"))
                    pop()
                }
                matchText.startsWith("*") && matchText.endsWith("*") -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(matchText.removeSurrounding("*"))
                    pop()
                }
            }
            lastIdx = match.range.last + 1
        }
        if (lastIdx < text.length) {
            append(text.substring(lastIdx))
        }
    }
}
