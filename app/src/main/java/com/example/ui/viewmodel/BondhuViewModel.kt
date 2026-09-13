package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.local.BondhuPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.StreamEvent
import com.example.data.remote.BondhuApiService
import com.example.ui.components.AttachedFileInfo
import com.example.ui.i18n.Strings
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppStage {
    SPLASH,
    ONBOARDING,
    CHAT
}

data class BondhuUiState(
    val stage: AppStage = AppStage.SPLASH,
    val language: String = "bn",
    val themeMode: String = "system", // "light", "dark", "system"
    val isDarkMode: Boolean = true,
    val selectedModel: String = "light", // "light" | "reasoning"
    val deviceId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val loginType: String = "guest",
    val credits: Int = 42,
    val isBanned: Boolean = false,
    val currentSessionId: String? = null,
    val activeSessionTitle: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isStreaming: Boolean = false,
    val isWakingUp: Boolean = false,
    val sessions: List<ChatSession> = emptyList(),
    val isHistoryDrawerOpen: Boolean = false,
    val isSettingsOpen: Boolean = false,
    val isModelSheetOpen: Boolean = false,
    val isWebSearchEnabled: Boolean = false,
    val isCustomApiEnabled: Boolean = false,
    val customApiEndpoint: String = "https://api.openai.com/v1",
    val customApiKey: String = "",
    val customApiModel: String = "gpt-4o-mini",
    val toastMessage: String? = null
)

class BondhuViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = BondhuPreferences(application)
    private val api = BondhuApiService()

    private val _uiState = MutableStateFlow(
        BondhuUiState(
            stage = AppStage.SPLASH,
            language = prefs.getLanguage(),
            themeMode = prefs.getTheme(),
            isDarkMode = prefs.isDarkMode(),
            selectedModel = prefs.getModel(),
            deviceId = prefs.getDeviceId(),
            userName = prefs.getUserName(),
            userEmail = prefs.getUserEmail(),
            loginType = prefs.getLoginType(),
            isCustomApiEnabled = prefs.isCustomApiEnabled(),
            customApiEndpoint = prefs.getCustomApiEndpoint(),
            customApiKey = prefs.getCustomApiKey(),
            customApiModel = prefs.getCustomApiModel()
        )
    )
    val uiState: StateFlow<BondhuUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var wakingUpJob: Job? = null

    init {
        loadCreditsAndSessions()
    }

    /**
     * Called when splash finishes:
     * New user -> Onboarding (4 steps)
     * Returning user -> Chat directly
     */
    fun onSplashComplete() {
        val hasOnboarded = prefs.isOnboardingCompleted()
        _uiState.update {
            it.copy(stage = if (hasOnboarded) AppStage.CHAT else AppStage.ONBOARDING)
        }
    }

    fun completeOnboarding(name: String) {
        val trimmed = name.trim().ifBlank { if (_uiState.value.language == "bn") "বন্ধু" else "Friend" }
        prefs.setUserName(trimmed)
        prefs.setOnboardingCompleted(true)
        _uiState.update {
            it.copy(
                userName = trimmed,
                stage = AppStage.CHAT
            )
        }
        loadCreditsAndSessions()
    }

    fun loadCreditsAndSessions() {
        val deviceId = _uiState.value.deviceId
        viewModelScope.launch {
            try {
                val credits = api.getCredits(deviceId)
                _uiState.update {
                    it.copy(
                        credits = credits.remainingCredits,
                        isBanned = credits.banned
                    )
                }
            } catch (_: Exception) {}

            try {
                val sessions = api.getSessions(deviceId)
                _uiState.update { it.copy(sessions = sessions) }
            } catch (_: Exception) {}
        }
    }

    fun saveUserName(name: String) {
        val clean = name.trim()
        prefs.setUserName(clean)
        _uiState.update { it.copy(userName = clean) }
    }

    fun setLanguage(lang: String) {
        prefs.setLanguage(lang)
        _uiState.update { it.copy(language = lang) }
    }

    fun setThemeMode(mode: String) {
        prefs.setTheme(mode)
        val isDark = when (mode) {
            "light" -> false
            "dark" -> true
            else -> true
        }
        _uiState.update { it.copy(themeMode = mode, isDarkMode = isDark) }
    }

    fun selectModel(model: String) {
        prefs.setModel(model)
        _uiState.update { it.copy(selectedModel = model) }
    }

    fun toggleWebSearch() {
        _uiState.update { it.copy(isWebSearchEnabled = !it.isWebSearchEnabled) }
    }

    fun setWebSearch(enabled: Boolean) {
        _uiState.update { it.copy(isWebSearchEnabled = enabled) }
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun openModelSheet(open: Boolean) {
        _uiState.update { it.copy(isModelSheetOpen = open) }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun openHistoryDrawer(open: Boolean) {
        _uiState.update { it.copy(isHistoryDrawerOpen = open) }
        if (open) {
            refreshSessions()
        }
    }

    fun startNewChat() {
        stopGeneration()
        _uiState.update {
            it.copy(
                currentSessionId = null,
                activeSessionTitle = null,
                messages = emptyList(),
                isHistoryDrawerOpen = false
            )
        }
    }

    fun loadSession(session: ChatSession) {
        stopGeneration()
        _uiState.update {
            it.copy(
                currentSessionId = session.id,
                activeSessionTitle = session.title,
                isHistoryDrawerOpen = false
            )
        }
        val deviceId = _uiState.value.deviceId
        viewModelScope.launch {
            try {
                val messages = api.getHistory(deviceId, session.id)
                _uiState.update { it.copy(messages = messages) }
            } catch (_: Exception) {
                showToast(Strings.errorGeneral(_uiState.value.language))
            }
        }
    }

    fun deleteSession(session: ChatSession) {
        val deviceId = _uiState.value.deviceId
        viewModelScope.launch {
            try {
                val deleted = api.deleteSession(deviceId, session.id)
                if (deleted) {
                    if (_uiState.value.currentSessionId == session.id) {
                        startNewChat()
                    }
                    refreshSessions()
                }
            } catch (_: Exception) {}
        }
    }

    private fun refreshSessions() {
        val deviceId = _uiState.value.deviceId
        viewModelScope.launch {
            try {
                val sessions = api.getSessions(deviceId)
                _uiState.update { it.copy(sessions = sessions) }
            } catch (_: Exception) {}
        }
    }

    fun deleteAllData() {
        stopGeneration()
        prefs.resetAll()
        _uiState.update {
            BondhuUiState(
                stage = AppStage.ONBOARDING,
                language = "bn",
                themeMode = "dark",
                isDarkMode = true,
                deviceId = prefs.getDeviceId()
            )
        }
    }

    fun setCustomApiConfig(enabled: Boolean, endpoint: String, apiKey: String, model: String) {
        prefs.setCustomApiEnabled(enabled)
        prefs.setCustomApiEndpoint(endpoint)
        prefs.setCustomApiKey(apiKey)
        prefs.setCustomApiModel(model)
        _uiState.update {
            it.copy(
                isCustomApiEnabled = enabled,
                customApiEndpoint = endpoint,
                customApiKey = apiKey,
                customApiModel = model
            )
        }
        showToast(Strings.apiConfigSaved(_uiState.value.language))
    }

    fun signInWithGoogleAccount(account: GoogleSignInAccount, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val name = account.displayName?.ifBlank { null } ?: "User"
            val email = account.email ?: ""

            // If Firebase is unavailable in this environment, complete with local profile
            if (AuthManager.auth == null) {
                prefs.setUserName(name)
                prefs.setUserEmail(email)
                prefs.setLoginType("google")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = name,
                        userEmail = email,
                        loginType = "google",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
                return@launch
            }

            val result = AuthManager.signInWithGoogleCredential(account)
            result.onSuccess { user ->
                val userName = user.displayName?.ifBlank { null } ?: name
                val userEmail = user.email?.ifBlank { null } ?: email
                prefs.setUserName(userName)
                prefs.setUserEmail(userEmail)
                prefs.setLoginType("google")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = userName,
                        userEmail = userEmail,
                        loginType = "google",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
            }.onFailure { _ ->
                // Graceful fallback to verified Google account data even if Firebase configuration is pending
                prefs.setUserName(name)
                prefs.setUserEmail(email)
                prefs.setLoginType("google")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = name,
                        userEmail = email,
                        loginType = "google",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
            }
        }
    }

    fun signInAnonymously(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            if (AuthManager.auth == null) {
                prefs.setLoginType("anonymous")
                prefs.setLoggedIn(true)
                _uiState.update { it.copy(loginType = "anonymous") }
                onResult(true, null)
                return@launch
            }
            val result = AuthManager.signInAnonymously()
            result.onSuccess {
                prefs.setLoginType("anonymous")
                prefs.setLoggedIn(true)
                _uiState.update { it.copy(loginType = "anonymous") }
                onResult(true, null)
            }.onFailure { err ->
                // Allow proceeding locally
                prefs.setLoginType("anonymous")
                prefs.setLoggedIn(true)
                _uiState.update { it.copy(loginType = "anonymous") }
                onResult(true, null)
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val fallbackName = email.substringBefore("@")
            if (AuthManager.auth == null) {
                // Local account fallback
                prefs.setUserName(fallbackName)
                prefs.setUserEmail(email)
                prefs.setLoginType("email")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = fallbackName,
                        userEmail = email,
                        loginType = "email",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
                return@launch
            }

            val result = AuthManager.signInWithEmail(email, pass)
            result.onSuccess { user ->
                val name = user.displayName?.ifBlank { null } ?: fallbackName
                val userEmail = user.email ?: email
                prefs.setUserName(name)
                prefs.setUserEmail(userEmail)
                prefs.setLoginType("email")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = name,
                        userEmail = userEmail,
                        loginType = "email",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Email sign-in failed")
            }
        }
    }

    fun signUpWithEmail(name: String, email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val fallbackName = name.ifBlank { email.substringBefore("@") }
            if (AuthManager.auth == null) {
                // Local account fallback
                prefs.setUserName(fallbackName)
                prefs.setUserEmail(email)
                prefs.setLoginType("email")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = fallbackName,
                        userEmail = email,
                        loginType = "email",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
                return@launch
            }

            val result = AuthManager.signUpWithEmail(name, email, pass)
            result.onSuccess { user ->
                val userName = user.displayName?.ifBlank { null } ?: fallbackName
                val userEmail = user.email ?: email
                prefs.setUserName(userName)
                prefs.setUserEmail(userEmail)
                prefs.setLoginType("email")
                prefs.setLoggedIn(true)
                prefs.setOnboardingCompleted(true)
                _uiState.update {
                    it.copy(
                        userName = userName,
                        userEmail = userEmail,
                        loginType = "email",
                        stage = AppStage.CHAT
                    )
                }
                loadCreditsAndSessions()
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.localizedMessage ?: "Sign-up failed")
            }
        }
    }

    fun logout(context: Context? = null) {
        stopGeneration()
        context?.let { AuthManager.signOut(it) } ?: run {
            try {
                AuthManager.auth?.signOut()
            } catch (_: Exception) {}
        }
        prefs.logout()
        _uiState.update {
            BondhuUiState(
                stage = AppStage.ONBOARDING,
                language = prefs.getLanguage(),
                themeMode = prefs.getTheme(),
                isDarkMode = prefs.isDarkMode(),
                deviceId = prefs.getDeviceId(),
                userName = "",
                userEmail = "",
                loginType = "guest",
                isCustomApiEnabled = prefs.isCustomApiEnabled(),
                customApiEndpoint = prefs.getCustomApiEndpoint(),
                customApiKey = prefs.getCustomApiKey(),
                customApiModel = prefs.getCustomApiModel()
            )
        }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
        viewModelScope.launch {
            delay(2400)
            _uiState.update {
                if (it.toastMessage == message) it.copy(toastMessage = null) else it
            }
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        streamJob = null
        wakingUpJob?.cancel()
        wakingUpJob = null

        _uiState.update { state ->
            val updated = state.messages.map { msg ->
                if (msg.isStreaming) msg.copy(isStreaming = false) else msg
            }
            state.copy(
                isStreaming = false,
                isWakingUp = false,
                messages = updated
            )
        }
    }

    fun sendMessage(promptText: String, attachment: AttachedFileInfo? = null) {
        if (_uiState.value.isStreaming) return

        val cleanPrompt = promptText.trim()
        if (cleanPrompt.isEmpty() && attachment == null) return

        val effectiveUserText = if (cleanPrompt.isEmpty() && attachment != null) {
            if (_uiState.value.language == "bn") "অনুগ্রহ করে এই ফাইলটি বিশ্লেষণ করে ব্যাখ্যা করো।"
            else "Please analyze this file and explain its contents."
        } else cleanPrompt

        val fullApiPrompt = if (attachment != null && !attachment.textContent.isNullOrBlank()) {
            """
            [File: ${attachment.name}]
            ${attachment.textContent}

            [Message]: $effectiveUserText
            """.trimIndent()
        } else if (attachment != null) {
            """
            [Attachment: ${attachment.name} (${attachment.sizeText})]

            [Message]: $effectiveUserText
            """.trimIndent()
        } else {
            effectiveUserText
        }

        val deviceId = _uiState.value.deviceId
        val sessionId = _uiState.value.currentSessionId

        val userMessage = ChatMessage(
            role = "user",
            content = effectiveUserText,
            attachmentName = attachment?.name,
            attachmentType = if (attachment?.isImage == true) "image" else "file"
        )
        val assistantMessage = ChatMessage(
            role = "assistant",
            content = "",
            isStreaming = true
        )

        _uiState.update { state ->
            state.copy(
                inputText = "",
                messages = state.messages + userMessage + assistantMessage,
                isStreaming = true,
                isWakingUp = false
            )
        }

        // Patient wake-up timer: show cold-start message if server takes >3.0s
        wakingUpJob?.cancel()
        wakingUpJob = viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(isWakingUp = true) }
        }

        streamJob?.cancel()
        val autoSearchQueries = listOf("today", "news", "current", "latest", "recent", "weather", "score", "খবর", "আজকের", "সংবাদ", "বর্তমান", "এখনকার", "তারিখ", "কালকে")
        val lowerPrompt = effectiveUserText.lowercase()
        val shouldSearch = _uiState.value.isWebSearchEnabled || autoSearchQueries.any { lowerPrompt.contains(it) }

        val isCustomApi = _uiState.value.isCustomApiEnabled && _uiState.value.customApiKey.isNotBlank()
        val streamFlow = if (isCustomApi) {
            api.sendCustomApiMessageStream(
                endpoint = _uiState.value.customApiEndpoint,
                apiKey = _uiState.value.customApiKey,
                model = _uiState.value.customApiModel,
                message = fullApiPrompt,
                history = _uiState.value.messages.dropLast(2)
            )
        } else {
            api.sendMessageStream(
                deviceId = deviceId,
                message = fullApiPrompt,
                sessionId = sessionId,
                model = _uiState.value.selectedModel,
                webSearch = shouldSearch
            )
        }

        streamJob = viewModelScope.launch {
            var currentContent = ""
            var receivedDelta = false

            try {
                streamFlow.collect { event ->
                    when (event) {
                        is StreamEvent.Meta -> {
                            wakingUpJob?.cancel()
                            _uiState.update { state ->
                                state.copy(
                                    currentSessionId = event.sessionId,
                                    credits = event.remainingCredits,
                                    isWakingUp = false
                                )
                            }
                        }

                        is StreamEvent.Delta -> {
                            wakingUpJob?.cancel()
                            receivedDelta = true
                            currentContent += event.text
                            _uiState.update { state ->
                                val lastIndex = state.messages.indexOfLast { it.isStreaming }
                                val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                                if (targetIndex != -1) {
                                    val updated = state.messages.toMutableList()
                                    updated[targetIndex] = updated[targetIndex].copy(
                                        content = currentContent,
                                        isStreaming = true,
                                        isError = false,
                                        errorMessage = null
                                    )
                                    state.copy(
                                        messages = updated,
                                        isStreaming = true,
                                        isWakingUp = false
                                    )
                                } else {
                                    state.copy(
                                        messages = state.messages + ChatMessage(
                                            role = "assistant",
                                            content = currentContent,
                                            isStreaming = true
                                        ),
                                        isStreaming = true,
                                        isWakingUp = false
                                    )
                                }
                            }
                        }

                        is StreamEvent.Done -> {
                            wakingUpJob?.cancel()
                            val finalReply = event.reply ?: currentContent
                            _uiState.update { state ->
                                val lastIndex = state.messages.indexOfLast { it.isStreaming }
                                val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                                if (targetIndex != -1) {
                                    val updated = state.messages.toMutableList()
                                    updated[targetIndex] = updated[targetIndex].copy(
                                        content = finalReply,
                                        isStreaming = false,
                                        isError = false
                                    )
                                    state.copy(
                                        messages = updated,
                                        isStreaming = false,
                                        isWakingUp = false,
                                        currentSessionId = event.sessionId ?: state.currentSessionId,
                                        credits = event.remainingCredits ?: state.credits
                                    )
                                } else {
                                    state.copy(isStreaming = false, isWakingUp = false)
                                }
                            }
                            if (!isCustomApi) {
                                refreshSessions()
                            }
                        }

                        is StreamEvent.Error -> {
                            wakingUpJob?.cancel()
                            val rawErr = event.error
                            val friendlyError = if (isCustomApi) rawErr else Strings.formatHttpError(rawErr, _uiState.value.language)
                            _uiState.update { state ->
                                val lastIndex = state.messages.indexOfLast { it.isStreaming }
                                val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                                if (targetIndex != -1) {
                                    val updated = state.messages.toMutableList()
                                    // If we had received delta, keep partial content
                                    val hadContent = currentContent.isNotBlank()
                                    updated[targetIndex] = updated[targetIndex].copy(
                                        content = if (hadContent) currentContent else friendlyError,
                                        isStreaming = false,
                                        isError = !hadContent,
                                        errorMessage = if (!hadContent) friendlyError else null,
                                        canRetry = true,
                                        retryPrompt = cleanPrompt
                                    )
                                    state.copy(
                                        messages = updated,
                                        isStreaming = false,
                                        isWakingUp = false
                                    )
                                } else {
                                    state.copy(isStreaming = false, isWakingUp = false)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                wakingUpJob?.cancel()
                val friendlyError = Strings.formatHttpError(e.message.orEmpty(), _uiState.value.language)
                _uiState.update { state ->
                    val lastIndex = state.messages.indexOfLast { it.isStreaming }
                    val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                    if (targetIndex != -1) {
                        val updated = state.messages.toMutableList()
                        val hadContent = currentContent.isNotBlank()
                        updated[targetIndex] = updated[targetIndex].copy(
                            content = if (hadContent) currentContent else friendlyError,
                            isStreaming = false,
                            isError = !hadContent,
                            errorMessage = if (!hadContent) friendlyError else null,
                            canRetry = true,
                            retryPrompt = cleanPrompt
                        )
                        state.copy(
                            messages = updated,
                            isStreaming = false,
                            isWakingUp = false
                        )
                    } else {
                        state.copy(isStreaming = false, isWakingUp = false)
                    }
                }
            } finally {
                _uiState.update { state ->
                    if (state.isStreaming) {
                        val updated = state.messages.map {
                            if (it.isStreaming) it.copy(isStreaming = false) else it
                        }
                        state.copy(isStreaming = false, isWakingUp = false, messages = updated)
                    } else state
                }
            }
        }
    }
}
