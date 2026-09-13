package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BondhuTheme
import com.example.ui.viewmodel.BondhuUiState
import com.example.ui.viewmodel.BondhuViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Main Chat Screen Composable:
 * - Top Bar: 56dp, [☰], "বন্ধু·AI", [✦ 42] credits pill, [＋] new chat
 * - History Drawer: 280dp from left, #141414, sessions list, user avatar footer
 * - Empty State: 48dp gold sun mark, "Hi, [name]!", "I'm Bondhu — your AI friend. Ask away.", 4 chips
 * - Message List: User (#22C55E right-aligned bubble), AI (no bg, 20dp sun mark, markdown, copy button)
 * - Typing Indicator: 20dp sun mark + 3 dots 8dp #22C55E staggered + "Bondhu is thinking…"
 * - Composer: Pill #1C1C1C 24dp radius, [📎 attach] [input] [⚡ model] [➤ send / ■ stop]
 * - Model Bottom Sheet & Settings Dialog
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatScreen(
    viewModel: BondhuViewModel,
    uiState: BondhuUiState,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Auto-scroll rule: Only if user is within 100dp of bottom
    val isNearBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf true

            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf true
            if (lastVisibleItem.index < totalItems - 1) {
                false
            } else {
                val thresholdPx = with(density) { 100.dp.toPx() }
                val bottomOffset = lastVisibleItem.offset + lastVisibleItem.size - layoutInfo.viewportEndOffset
                bottomOffset <= thresholdPx
            }
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.messages.lastOrNull()?.content) {
        if (isNearBottom && uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    // Auto-scroll to latest message when keyboard pops up so the writing/reading area is never covered
    val isImeVisible = WindowInsets.isImeVisible
    LaunchedEffect(isImeVisible) {
        if (isImeVisible && uiState.messages.isNotEmpty()) {
            delay(150)
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

    val colors = BondhuTheme.colors

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colors.surface,
                drawerShape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
            ) {
                HistoryDrawerContent(
                    sessions = uiState.sessions,
                    currentSessionId = uiState.currentSessionId,
                    language = uiState.language,
                    userName = uiState.userName,
                    userEmail = uiState.userEmail,
                    loginType = uiState.loginType,
                    onNewChatClick = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onSessionClick = { session ->
                        viewModel.loadSession(session)
                        scope.launch { drawerState.close() }
                    },
                    onDeleteSessionClick = { session ->
                        viewModel.deleteSession(session)
                    },
                    onSectionPromptClick = { prompt ->
                        viewModel.sendMessage(prompt)
                        scope.launch { drawerState.close() }
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = colors.background,
            topBar = {
                TopBar(
                    credits = uiState.credits,
                    language = uiState.language,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onNewChatClick = { viewModel.startNewChat() }
                )
            },
            bottomBar = {
                ComposerView(
                    text = uiState.inputText,
                    onTextChange = { viewModel.onInputTextChanged(it) },
                    isStreaming = uiState.isStreaming,
                    currentModel = uiState.selectedModel,
                    language = uiState.language,
                    isWebSearchEnabled = uiState.isWebSearchEnabled,
                    onToggleWebSearch = { viewModel.toggleWebSearch() },
                    onSend = { message, attachment ->
                        viewModel.sendMessage(message, attachment)
                    },
                    onStop = { viewModel.stopGeneration() },
                    onOpenModelSelector = { viewModel.openModelSheet(true) }
                )
            },
            modifier = modifier
                .fillMaxSize()
                .testTag("chat_screen")
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(colors.background)
            ) {
                if (uiState.messages.isEmpty()) {
                    EmptyStateView(
                        userName = uiState.userName,
                        language = uiState.language,
                        onSuggestionClick = { prompt ->
                            viewModel.sendMessage(prompt)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.messages, key = { it.id }) { message ->
                            ChatMessageItem(
                                message = message,
                                language = uiState.language,
                                isColdStart = uiState.isWakingUp,
                                onRetry = { retryPrompt ->
                                    viewModel.sendMessage(retryPrompt)
                                }
                            )
                        }

                        if (uiState.isStreaming && uiState.messages.none { it.role == "assistant" && it.isStreaming }) {
                            item(key = "typing_indicator_item") {
                                TypingIndicator(
                                    isColdStart = uiState.isWakingUp,
                                    language = uiState.language
                                )
                            }
                        }

                        item(key = "bottom_spacer") {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    // Model Selector Bottom Sheet
    if (uiState.isModelSheetOpen) {
        ModelBottomSheet(
            selectedModel = uiState.selectedModel,
            language = uiState.language,
            onModelSelect = { model ->
                viewModel.selectModel(model)
            },
            onDismiss = { viewModel.openModelSheet(false) }
        )
    }

    val context = LocalContext.current

    // Settings Dialog
    if (uiState.isSettingsOpen) {
        SettingsDialog(
            language = uiState.language,
            themeMode = uiState.themeMode,
            userName = uiState.userName,
            userEmail = uiState.userEmail,
            loginType = uiState.loginType,
            currentModel = uiState.selectedModel,
            fontSize = "normal",
            animationsEnabled = true,
            isCustomApiEnabled = uiState.isCustomApiEnabled,
            customApiEndpoint = uiState.customApiEndpoint,
            customApiKey = uiState.customApiKey,
            customApiModel = uiState.customApiModel,
            onLanguageChange = { lang -> viewModel.setLanguage(lang) },
            onThemeChange = { mode -> viewModel.setThemeMode(mode) },
            onSaveName = { name -> viewModel.saveUserName(name) },
            onModelChange = { model -> viewModel.selectModel(model) },
            onConnectGoogle = { account ->
                viewModel.signInWithGoogleAccount(account) { _, _ -> }
            },
            onSaveCustomApi = { enabled, endpoint, key, model ->
                viewModel.setCustomApiConfig(enabled, endpoint, key, model)
            },
            onLogout = { viewModel.logout(context) },
            onDismiss = { viewModel.openSettings(false) }
        )
    }
}
