package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BondhuPreferences
import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.StreamEvent
import com.example.data.remote.BondhuApiService
import com.example.ui.i18n.Strings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppStage {
    SPLASH,
    LANGUAGE_SELECT,
    CHAT
}

data class BondhuUiState(
    val stage: AppStage = AppStage.SPLASH,
    val language: String = "en",
    val isDarkMode: Boolean = false,
    val deviceId: String = "",
    val credits: Int = 50,
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
    val isCreditsInfoOpen: Boolean = false,
    val sessionToDelete: ChatSession? = null,
    val toastMessage: String? = null,
    val selectedModel: String = "Bondhu-5.3",
    val isCookingSoonDialogOpen: Boolean = false
)

class BondhuViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = BondhuPreferences(application)
    private val api = BondhuApiService()

    private val _uiState = MutableStateFlow(
        BondhuUiState(
            language = prefs.getLanguage(),
            isDarkMode = prefs.isDarkMode(),
            deviceId = prefs.getDeviceId()
        )
    )
    val uiState: StateFlow<BondhuUiState> = _uiState.asStateFlow()

    private var streamJob: Job? = null
    private var wakingUpJob: Job? = null

    init {
        // Splash sequence with cinematic rising red sun (~2.8s)
        viewModelScope.launch {
            delay(2800)
            _uiState.update { current ->
                current.copy(stage = AppStage.CHAT)
            }
        }

        // Fetch initial credits and sessions
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

    fun selectInitialLanguage(lang: String) {
        prefs.setLanguage(lang)
        _uiState.update {
            it.copy(
                language = lang,
                stage = AppStage.CHAT,
                toastMessage = Strings.welcomeToast(lang)
            )
        }
    }

    fun setLanguage(lang: String) {
        prefs.setLanguage(lang)
        _uiState.update { it.copy(language = lang) }
    }

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        prefs.setDarkMode(newMode)
        _uiState.update { it.copy(isDarkMode = newMode) }
    }

    fun onInputTextChanged(text: String) {
        if (text.length <= 4000) {
            _uiState.update { it.copy(inputText = text) }
        }
    }

    fun fillComposer(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun showToast(message: String) {
        _uiState.update { it.copy(toastMessage = message) }
        viewModelScope.launch {
            delay(2500)
            _uiState.update {
                if (it.toastMessage == message) it.copy(toastMessage = null) else it
            }
        }
    }

    fun clearToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    fun openHistoryDrawer(open: Boolean) {
        _uiState.update { it.copy(isHistoryDrawerOpen = open) }
        if (open) {
            refreshSessions()
        }
    }

    fun openSettings(open: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = open) }
    }

    fun openCreditsInfo(open: Boolean) {
        _uiState.update { it.copy(isCreditsInfoOpen = open) }
    }

    fun setSessionToDelete(session: ChatSession?) {
        _uiState.update { it.copy(sessionToDelete = session) }
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
            } catch (e: Exception) {
                showToast(Strings.errorGeneral(_uiState.value.language))
            }
        }
    }

    fun confirmDeleteSession() {
        val session = _uiState.value.sessionToDelete ?: return
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
            _uiState.update { it.copy(sessionToDelete = null) }
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

    fun stopGeneration() {
        streamJob?.cancel()
        streamJob = null
        wakingUpJob?.cancel()
        wakingUpJob = null

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.isStreaming) msg.copy(isStreaming = false) else msg
            }
            state.copy(
                isStreaming = false,
                isWakingUp = false,
                messages = updatedMessages
            )
        }
    }

    fun sendMessage(retryPrompt: String? = null) {
        if (_uiState.value.isStreaming) return

        val prompt = retryPrompt ?: _uiState.value.inputText.trim()
        if (prompt.isEmpty()) return

        val lang = _uiState.value.language
        val deviceId = _uiState.value.deviceId
        val sessionId = _uiState.value.currentSessionId

        // Add user message
        val userMsg = ChatMessage(role = "user", content = prompt)
        val assistantMsg = ChatMessage(
            role = "assistant",
            content = "",
            isStreaming = true
        )

        _uiState.update { state ->
            state.copy(
                inputText = if (retryPrompt == null) "" else state.inputText,
                messages = state.messages + userMsg + assistantMsg,
                isStreaming = true,
                isWakingUp = false
            )
        }

        // Launch patient wake up timer: if backend takes > 3.5s before sending first event, show "waking up"
        wakingUpJob?.cancel()
        wakingUpJob = viewModelScope.launch {
            delay(3500)
            _uiState.update { it.copy(isWakingUp = true) }
        }

        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            var receivedAnyDelta = false
            var currentContent = ""

            try {
                api.sendMessageStream(deviceId, prompt, sessionId).collect { event ->
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
                            receivedAnyDelta = true
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
                            val finalContent = if (!event.reply.isNullOrEmpty()) event.reply else currentContent
                            _uiState.update { state ->
                                val lastIndex = state.messages.indexOfLast { it.isStreaming }
                                val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                                val updated = state.messages.toMutableList()
                                if (targetIndex != -1) {
                                    updated[targetIndex] = updated[targetIndex].copy(
                                        content = finalContent,
                                        isStreaming = false,
                                        isError = false,
                                        errorMessage = null
                                    )
                                } else {
                                    updated.add(
                                        ChatMessage(
                                            role = "assistant",
                                            content = finalContent,
                                            isStreaming = false
                                        )
                                    )
                                }
                                val newSessionId = event.sessionId ?: state.currentSessionId
                                val newCredits = event.remainingCredits ?: state.credits
                                state.copy(
                                    messages = updated,
                                    isStreaming = false,
                                    isWakingUp = false,
                                    currentSessionId = newSessionId,
                                    credits = newCredits
                                )
                            }
                            refreshSessions()
                        }

                        is StreamEvent.Error -> {
                            wakingUpJob?.cancel()
                            val errStr = event.error
                            val localizedError = when (errStr) {
                                "HTTP_400" -> Strings.error400(lang)
                                "HTTP_402" -> Strings.error402(lang)
                                "HTTP_403" -> Strings.error403(lang)
                                "HTTP_429" -> Strings.error429(lang)
                                "HTTP_503" -> Strings.error503(lang)
                                "STREAM_DROPPED" -> Strings.errorGeneral(lang)
                                else -> errStr
                            }

                            _uiState.update { state ->
                                val lastIndex = state.messages.indexOfLast { it.isStreaming }
                                val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                                val updated = state.messages.toMutableList()
                                if (targetIndex != -1) {
                                    if (receivedAnyDelta) {
                                        // Keep all received text intact
                                        updated[targetIndex] = updated[targetIndex].copy(
                                            content = currentContent,
                                            isStreaming = false,
                                            isError = false
                                        )
                                    } else {
                                        // Full error bubble only if no text was received
                                        updated[targetIndex] = updated[targetIndex].copy(
                                            content = localizedError,
                                            isStreaming = false,
                                            isError = true,
                                            canRetry = true,
                                            retryPrompt = prompt
                                        )
                                    }
                                }
                                state.copy(
                                    messages = updated,
                                    isStreaming = false,
                                    isWakingUp = false
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                wakingUpJob?.cancel()
                _uiState.update { state ->
                    val lastIndex = state.messages.indexOfLast { it.isStreaming }
                    val targetIndex = if (lastIndex != -1) lastIndex else state.messages.indexOfLast { it.role == "assistant" }
                    val updated = state.messages.toMutableList()
                    if (targetIndex != -1) {
                        if (receivedAnyDelta) {
                            updated[targetIndex] = updated[targetIndex].copy(
                                content = currentContent,
                                isStreaming = false,
                                isError = false
                            )
                        } else {
                            updated[targetIndex] = updated[targetIndex].copy(
                                isStreaming = false,
                                isError = true,
                                errorMessage = Strings.errorGeneral(lang),
                                canRetry = true,
                                retryPrompt = prompt
                            )
                        }
                    }
                    state.copy(
                        messages = updated,
                        isStreaming = false,
                        isWakingUp = false
                    )
                }
            } finally {
                wakingUpJob?.cancel()
                _uiState.update { state ->
                    val lastStreaming = state.messages.indexOfLast { it.isStreaming }
                    if (lastStreaming != -1) {
                        val updated = state.messages.toMutableList()
                        val msg = updated[lastStreaming]
                        updated[lastStreaming] = msg.copy(
                            content = if (msg.content.isNotEmpty()) msg.content else currentContent,
                            isStreaming = false
                        )
                        state.copy(messages = updated, isStreaming = false, isWakingUp = false)
                    } else {
                        state.copy(isStreaming = false, isWakingUp = false)
                    }
                }
            }
        }
    }

    fun selectModel(model: String) {
        _uiState.update {
            it.copy(selectedModel = model)
        }
        showToast("Model switched to $model")
    }

    fun openCookingSoon(open: Boolean) {
        _uiState.update {
            it.copy(isCookingSoonDialogOpen = open)
        }
    }
}
