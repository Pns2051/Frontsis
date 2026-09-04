package com.example.data.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false,
    val retryPrompt: String? = null
)

data class ChatSession(
    val id: String,
    val title: String,
    val createdAt: String? = null,
    val messageCount: Int = 0
)

data class CreditsInfo(
    val remainingCredits: Int = 50,
    val banned: Boolean = false,
    val dailyRefillApplied: Boolean = false
)

sealed class StreamEvent {
    data class Meta(
        val sessionId: String,
        val remainingCredits: Int,
        val dailyRefillApplied: Boolean? = null
    ) : StreamEvent()

    data class Delta(val text: String) : StreamEvent()

    data class Done(
        val reply: String?,
        val sessionId: String?,
        val remainingCredits: Int?,
        val persisted: Boolean? = null
    ) : StreamEvent()

    data class Error(val error: String) : StreamEvent()
}
