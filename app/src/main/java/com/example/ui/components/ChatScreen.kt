package com.example.ui.components

import android.content.Intent
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.i18n.Strings
import com.example.ui.viewmodel.BondhuUiState
import com.example.ui.viewmodel.BondhuViewModel
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: BondhuViewModel,
    uiState: BondhuUiState,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var isApiDialogOpen by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        var ttsInstance: TextToSpeech? = null
        ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts = ttsInstance
                ttsInstance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {}
                    override fun onDone(utteranceId: String?) {
                        speakingMessageId = null
                    }
                    override fun onError(utteranceId: String?) {
                        speakingMessageId = null
                    }
                })
            }
        }
        onDispose {
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        }
    }

    val shareText: (String, String) -> Unit = { textToShare, chooserTitle ->
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textToShare)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, chooserTitle)
            context.startActivity(shareIntent)
        } catch (_: Exception) {}
    }

    val shareConversation: () -> Unit = {
        if (uiState.messages.isNotEmpty()) {
            val sb = StringBuilder()
            sb.append("=== ${Strings.appName(uiState.language)} ===\n\n")
            uiState.messages.forEach { msg ->
                val speaker = if (msg.role == "user") "User" else "Bondhu AI"
                sb.append("[$speaker]:\n${msg.content}\n\n")
            }
            shareText(sb.toString(), Strings.shareConversation(uiState.language))
        }
    }

    val toggleSpeak: (com.example.data.model.ChatMessage) -> Unit = { msg ->
        if (speakingMessageId == msg.id) {
            tts?.stop()
            speakingMessageId = null
        } else {
            tts?.stop()
            val langLocale = if (uiState.language == "bn") Locale("bn", "BD") else Locale.US
            val res = tts?.setLanguage(langLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            val cleanSpeech = msg.content
                .replace(Regex("```[\\s\\S]*?```"), " code block ")
                .replace(Regex("[#*_`\\[\\]]"), "")
                .trim()
            if (cleanSpeech.isNotBlank()) {
                val speakResult = tts?.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, msg.id)
                if (speakResult == TextToSpeech.SUCCESS) {
                    speakingMessageId = msg.id
                } else {
                    viewModel.showToast(Strings.speechNotAvailable(uiState.language))
                }
            }
        }
    }

    // Auto-scroll only if user is already near bottom
    val isNearBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf true
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= totalItems - 2
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (isNearBottom && uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Sync drawer open state with uiState
    LaunchedEffect(uiState.isHistoryDrawerOpen) {
        if (uiState.isHistoryDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!uiState.isHistoryDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen != uiState.isHistoryDrawerOpen) {
            viewModel.openHistoryDrawer(drawerState.isOpen)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                HistoryDrawerContent(
                    sessions = uiState.sessions,
                    currentSessionId = uiState.currentSessionId,
                    language = uiState.language,
                    onNewChatClick = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onSessionClick = { session ->
                        viewModel.loadSession(session)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSessionClick = { session ->
                        viewModel.setSessionToDelete(session)
                    },
                    onSettingsClick = {
                        viewModel.openSettings(true)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    credits = uiState.credits,
                    language = uiState.language,
                    modelName = uiState.selectedModel,
                    onModelSelect = { viewModel.selectModel(it) },
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNewChatClick = { viewModel.startNewChat() },
                    onCreditsClick = { viewModel.openCreditsInfo(true) },
                    onApiClick = { isApiDialogOpen = true },
                    onShareConversation = if (uiState.messages.isNotEmpty()) {
                        { shareConversation() }
                    } else null
                )
            },
            bottomBar = {
                ComposerView(
                    text = uiState.inputText,
                    onTextChanged = { viewModel.onInputTextChanged(it) },
                    onSend = { viewModel.sendMessage() },
                    onStop = { viewModel.stopGeneration() },
                    onAttachFileClick = {
                        viewModel.openCookingSoon(true)
                    },
                    isStreaming = uiState.isStreaming,
                    language = uiState.language,
                    onSpeechError = {
                        viewModel.showToast(Strings.speechNotAvailable(uiState.language))
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (uiState.messages.isEmpty()) {
                    // Empty state with animated greeting & suggestions
                    EmptyStateView(
                        language = uiState.language,
                        onSuggestionClick = { suggestion ->
                            viewModel.fillComposer(suggestion)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Messages list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            val lastAssistantIndex = uiState.messages.indexOfLast { it.role == "assistant" }
                            val isLastAssistant = uiState.messages.indexOf(message) == lastAssistantIndex
                            val lastUserMsg = if (isLastAssistant) {
                                uiState.messages.take(lastAssistantIndex).lastOrNull { it.role == "user" }
                            } else null

                            MarkdownMessageView(
                                message = message,
                                language = uiState.language,
                                isSpeaking = speakingMessageId == message.id,
                                onToggleSpeak = { toggleSpeak(message) },
                                onShare = { shareText(message.content, Strings.share(uiState.language)) },
                                onRegenerate = if (isLastAssistant && !uiState.isStreaming && lastUserMsg != null) {
                                    { viewModel.sendMessage(lastUserMsg.content) }
                                } else null,
                                onRetry = { retryPrompt ->
                                    viewModel.sendMessage(retryPrompt)
                                },
                                onCopyToast = {
                                    viewModel.showToast(Strings.copied(uiState.language))
                                }
                            )
                        }

                        // Waking up or typing indicator if assistant message is empty and streaming
                        val lastMsg = uiState.messages.lastOrNull()
                        if (uiState.isStreaming && lastMsg != null && lastMsg.role == "assistant" && lastMsg.content.isEmpty()) {
                            item(key = "typing_indicator_item") {
                                Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                    TypingIndicator(
                                        language = uiState.language,
                                        isWakingUp = uiState.isWakingUp
                                    )
                                }
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Polished Bottom Toast Notification
                AnimatedVisibility(
                    visible = uiState.toastMessage != null,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                ) {
                    uiState.toastMessage?.let { msg ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .shadow(8.dp, RoundedCornerShape(20.dp))
                                .background(Color(0xFF1E2823))
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                                .testTag("app_toast")
                        ) {
                            Text(
                                text = msg,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            language = uiState.language,
            isDarkMode = uiState.isDarkMode,
            credits = uiState.credits,
            onLanguageChange = { viewModel.setLanguage(it) },
            onToggleDarkMode = { viewModel.toggleDarkMode() },
            onDismiss = { viewModel.openSettings(false) }
        )
    }

    // Credits Info Dialog (when clicking credits pill)
    if (uiState.isCreditsInfoOpen) {
        Dialog(onDismissRequest = { viewModel.openCreditsInfo(false) }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
                    .testTag("credits_info_dialog")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Strings.creditsLabel(uiState.language),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(
                            onClick = { viewModel.openCreditsInfo(false) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    CreditsCardSection(credits = uiState.credits, language = uiState.language)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (uiState.sessionToDelete != null) {
        DeleteConfirmDialog(
            language = uiState.language,
            onConfirm = { viewModel.confirmDeleteSession() },
            onDismiss = { viewModel.setSessionToDelete(null) }
        )
    }

    // API Information Dialog
    if (isApiDialogOpen) {
        ApiInfoDialog(
            onDismiss = { isApiDialogOpen = false },
            onCopied = { viewModel.showToast("API URL copied to clipboard") }
        )
    }

    // Cooking Soon Dialog for Uploads
    if (uiState.isCookingSoonDialogOpen) {
        CookingSoonDialog(
            onDismiss = { viewModel.openCookingSoon(false) }
        )
    }
}
