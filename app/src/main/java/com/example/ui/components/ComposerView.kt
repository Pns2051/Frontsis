package com.example.ui.components

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.Strings
import com.example.ui.theme.BengalGreen
import com.example.ui.theme.SunriseRed

@Composable
fun ComposerView(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onAttachFileClick: () -> Unit,
    isStreaming: Boolean,
    language: String,
    onSpeechError: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val canSend = text.trim().isNotEmpty() && !isStreaming
    var lastSendClickTime by remember { mutableLongStateOf(0L) }
    var isWebSearchActive by remember { mutableStateOf(false) }
    var isAttachPressed by remember { mutableStateOf(false) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val newText = if (text.isBlank()) spokenText else "$text $spokenText"
                onTextChanged(newText)
            }
        }
    }

    val safeSend = {
        val now = System.currentTimeMillis()
        if (now - lastSendClickTime > 400L && canSend) {
            lastSendClickTime = now
            onSend()
        }
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (text.isNotEmpty()) BengalGreen.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
        animationSpec = tween(250),
        label = "composer_border"
    )

    val sendButtonScale by animateFloatAsState(
        targetValue = if (canSend) 1f else 0.88f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "send_button_scale"
    )

    val sendButtonBg by animateColorAsState(
        targetValue = if (canSend) BengalGreen
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "send_button_bg"
    )

    val attachRotation by animateFloatAsState(
        targetValue = if (isAttachPressed) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "attach_rotation"
    )

    val webSearchScale by animateFloatAsState(
        targetValue = if (isWebSearchActive) 1.08f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "web_search_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("composer_container")
    ) {
        // Floating Input Card matching the reference screenshot
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 3.dp,
                    shape = RoundedCornerShape(20.dp),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = animatedBorderColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Text Input Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = 130.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = Strings.composerPlaceholder(language),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = {
                        if (it.length <= 2000) {
                            onTextChanged(it)
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Normal,
                        lineHeight = 21.sp
                    ),
                    cursorBrush = SolidColor(BengalGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("message_input")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left tools: Plus icon (Cooking Soon) & Web Search icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Plus Attach Icon (+ Cooking Soon)
                    IconButton(
                        onClick = {
                            isAttachPressed = !isAttachPressed
                            onAttachFileClick()
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .testTag("attach_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach File (Cooking Soon)",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier
                                .size(19.dp)
                                .rotate(attachRotation)
                        )
                    }

                    // Web Search Icon (Globe)
                    IconButton(
                        onClick = { isWebSearchActive = !isWebSearchActive },
                        modifier = Modifier
                            .size(34.dp)
                            .scale(webSearchScale)
                            .clip(CircleShape)
                            .background(
                                if (isWebSearchActive) BengalGreen.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                            .testTag("web_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Web Search",
                            tint = if (isWebSearchActive) BengalGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Right tools: Character counter (if long) & Upward Arrow Send Button (↑) or Stop (■)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Character Count warning if near limit
                    if (text.length > 1500) {
                        Text(
                            text = Strings.charCountLimit(text.length, 2000, language),
                            fontSize = 11.sp,
                            color = if (text.length > 1950) SunriseRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear text button if text is not empty
                    if (text.isNotEmpty() && !isStreaming) {
                        IconButton(
                            onClick = { onTextChanged("") },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .testTag("clear_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear text",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Voice Input (Microphone) Button
                    if (!isStreaming) {
                        IconButton(
                            onClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (language == "bn") "bn-BD" else "en-US")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, Strings.voiceInput(language))
                                    }
                                    speechRecognizerLauncher.launch(intent)
                                } catch (_: Exception) {
                                    onSpeechError()
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                .testTag("voice_input_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = Strings.voiceInput(language),
                                tint = BengalGreen,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    // Upward Arrow Send Button (↑) or Stop (■ / ✕)
                    AnimatedContent(
                        targetState = isStreaming,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "sendStopButton"
                    ) { streaming ->
                        if (streaming) {
                            // Stop Generation Button
                            IconButton(
                                onClick = onStop,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(SunriseRed)
                                    .testTag("stop_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Stop generation",
                                    tint = Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        } else {
                            // Upward Arrow Send Button
                            IconButton(
                                onClick = { safeSend() },
                                enabled = canSend,
                                modifier = Modifier
                                    .size(34.dp)
                                    .scale(sendButtonScale)
                                    .clip(CircleShape)
                                    .background(sendButtonBg)
                                    .testTag("send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Send Message",
                                    tint = if (canSend) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
