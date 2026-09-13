package com.example.data.remote

import com.example.data.model.ChatMessage
import com.example.data.model.ChatSession
import com.example.data.model.CreditsInfo
import com.example.data.model.StreamEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class BondhuApiService {
    private val baseUrl = "https://bondhu-ai-backed-beta26.onrender.com"
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // 120 second read timeout because free Render backends can take up to a minute on cold start
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Sends message and returns a Flow of StreamEvents.
     * Can be cancelled by cancelling the coroutine scope collecting the flow.
     */
    fun sendMessageStream(
        deviceId: String,
        message: String,
        sessionId: String?,
        model: String = "light",
        webSearch: Boolean = false
    ): Flow<StreamEvent> = callbackFlow {
        val jsonBody = JSONObject().apply {
            put("device_id", deviceId)
            put("message", message)
            if (!sessionId.isNullOrBlank()) {
                put("session_id", sessionId)
            }
            if (model == "reasoning") {
                put("model", "reasoning")
            }
            if (webSearch) {
                put("web_search", true)
            }
        }

        val request = Request.Builder()
            .url("$baseUrl/api/chat")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        val call: Call = client.newCall(request)

        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) {
                    trySend(StreamEvent.Error(e.message ?: "Connection error"))
                    close(e)
                } else {
                    close()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string().orEmpty()
                    var errorMsg = ""
                    try {
                        val obj = JSONObject(errorBody)
                        errorMsg = obj.optString("error", "")
                    } catch (_: Exception) {}

                    val finalMsg = when (code) {
                        400 -> if (errorMsg.isNotBlank()) errorMsg else "HTTP_400"
                        402 -> "HTTP_402"
                        403 -> "HTTP_403"
                        429 -> "HTTP_429"
                        503 -> "HTTP_503"
                        else -> if (errorMsg.isNotBlank()) errorMsg else "HTTP_$code"
                    }
                    trySend(StreamEvent.Error(finalMsg))
                    close()
                    return
                }

                val body = response.body
                if (body == null) {
                    trySend(StreamEvent.Error("Empty response"))
                    close()
                    return
                }

                try {
                    val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                    var currentEventName: String? = null
                    val dataBuffer = StringBuilder()

                    var line: String? = reader.readLine()
                    while (line != null) {
                        // Lines starting with ':' are heartbeats
                        if (line.startsWith(":")) {
                            line = reader.readLine()
                            continue
                        }

                        if (line.startsWith("event:")) {
                            currentEventName = line.substring(6).trim()
                        } else if (line.startsWith("data:")) {
                            val dataPart = if (line.startsWith("data: ")) {
                                line.substring(6)
                            } else {
                                line.substring(5)
                            }
                            if (dataBuffer.isNotEmpty()) {
                                dataBuffer.append("\n")
                            }
                            dataBuffer.append(dataPart)
                        } else if (line.isEmpty()) {
                            // Frame complete on empty line (\n\n)
                            if (dataBuffer.isNotEmpty()) {
                                dispatchEvent(currentEventName, dataBuffer.toString()) { event ->
                                    trySend(event)
                                }
                            }
                            currentEventName = null
                            dataBuffer.setLength(0)
                        }

                        line = reader.readLine()
                    }

                    // Process any trailing frame
                    if (dataBuffer.isNotEmpty()) {
                        dispatchEvent(currentEventName, dataBuffer.toString()) { event ->
                            trySend(event)
                        }
                    }

                    close()
                } catch (e: Exception) {
                    if (!call.isCanceled()) {
                        trySend(StreamEvent.Error("STREAM_DROPPED"))
                    }
                    close()
                } finally {
                    body.close()
                }
            }
        })

        awaitClose {
            call.cancel()
        }
    }

    private fun dispatchEvent(
        eventName: String?,
        dataJson: String,
        emit: (StreamEvent) -> Unit
    ) {
        val trimmed = dataJson.trim()
        if (trimmed.isEmpty()) return

        try {
            val json = JSONObject(trimmed)
            // 1. Delta event: matches "event: delta" OR payload containing "text"
            if (eventName == "delta" || (json.has("text") && !json.has("reply") && !json.has("usage"))) {
                val text = if (json.has("text") && !json.isNull("text")) {
                    json.optString("text", "")
                } else {
                    ""
                }
                emit(StreamEvent.Delta(text))
                return
            }

            // 2. Done event: matches "event: done" OR payload containing "reply" or "usage"
            if (eventName == "done" || json.has("reply") || json.has("usage")) {
                val reply = if (json.has("reply") && !json.isNull("reply")) {
                    json.getString("reply")
                } else null
                val sessionId = when {
                    json.has("session_id") && !json.isNull("session_id") -> json.getString("session_id")
                    json.has("conversation_id") && !json.isNull("conversation_id") -> json.getString("conversation_id")
                    else -> null
                }
                val remainingCredits = if (json.has("remaining_credits")) json.optInt("remaining_credits") else null
                val persisted = if (json.has("persisted")) json.optBoolean("persisted") else null
                emit(StreamEvent.Done(reply, sessionId, remainingCredits, persisted))
                return
            }

            // 3. Meta event: matches "event: meta" OR payload containing session_id/conversation_id/remaining_credits
            if (eventName == "meta" || ((json.has("session_id") || json.has("conversation_id") || json.has("remaining_credits")) && !json.has("text") && !json.has("reply"))) {
                val sessionId = when {
                    json.has("session_id") -> json.optString("session_id", "")
                    json.has("conversation_id") -> json.optString("conversation_id", "")
                    else -> ""
                }
                val remainingCredits = json.optInt("remaining_credits", 0)
                val dailyRefill = if (json.has("daily_refill_applied")) json.optBoolean("daily_refill_applied") else null
                emit(StreamEvent.Meta(sessionId, remainingCredits, dailyRefill))
                return
            }

            // 4. Error event: matches "event: error" OR payload containing "error" or "message"
            if (eventName == "error" || json.has("error") || (json.has("message") && !json.has("text"))) {
                val err = when {
                    json.has("error") -> json.optString("error", "Error")
                    json.has("message") -> json.optString("message", "Error")
                    else -> "Error"
                }
                emit(StreamEvent.Error(err))
                return
            }

            // Fallback for json with text
            if (json.has("text")) {
                emit(StreamEvent.Delta(json.optString("text", "")))
            }
        } catch (e: Exception) {
            // If delta data was raw string
            if (eventName == "done") {
                emit(StreamEvent.Done(reply = trimmed, sessionId = null, remainingCredits = null, persisted = null))
            } else {
                emit(StreamEvent.Delta(trimmed))
            }
        }
    }

    suspend fun getCredits(deviceId: String): CreditsInfo = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/credits?device_id=$deviceId")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                return@withContext CreditsInfo()
            }
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            CreditsInfo(
                remainingCredits = json.optInt("remaining_credits", 50),
                banned = json.optBoolean("banned", false),
                dailyRefillApplied = json.optBoolean("daily_refill_applied", false)
            )
        }
    }

    suspend fun getSessions(deviceId: String): List<ChatSession> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/sessions?device_id=$deviceId")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val array = json.optJSONArray("sessions") ?: return@withContext emptyList()
            val list = mutableListOf<ChatSession>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    ChatSession(
                        id = item.optString("id", ""),
                        title = item.optString("title", "Conversation"),
                        createdAt = item.optString("created_at", null),
                        messageCount = item.optInt("message_count", 0)
                    )
                )
            }
            // Sort by created_at descending
            list.sortedByDescending { it.createdAt.orEmpty() }
        }
    }

    suspend fun getHistory(deviceId: String, sessionId: String): List<ChatMessage> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/history?device_id=$deviceId&session_id=$sessionId")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext emptyList()
            val body = response.body?.string().orEmpty()
            val json = JSONObject(body)
            val array = json.optJSONArray("messages") ?: return@withContext emptyList()
            val list = mutableListOf<ChatMessage>()
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(
                    ChatMessage(
                        role = item.optString("role", "assistant"),
                        content = item.optString("content", "")
                    )
                )
            }
            list
        }
    }

    suspend fun deleteSession(deviceId: String, sessionId: String): Boolean = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$baseUrl/api/sessions/$sessionId?device_id=$deviceId")
            .delete()
            .build()

        client.newCall(request).execute().use { response ->
            response.isSuccessful
        }
    }

    /**
     * Streams chat completions from a user-configured custom AI API.
     * Supports OpenAI-compatible endpoints (OpenAI, Groq, OpenRouter, Together, DeepSeek, Ollama)
     * and Google Gemini endpoints.
     */
    fun sendCustomApiMessageStream(
        endpoint: String,
        apiKey: String,
        model: String,
        message: String,
        history: List<ChatMessage>
    ): Flow<StreamEvent> = callbackFlow {
        val cleanEndpoint = endpoint.trim().trimEnd('/')
        val isGemini = cleanEndpoint.contains("generativelanguage.googleapis.com")

        val request = if (isGemini) {
            val url = if (cleanEndpoint.contains(":streamGenerateContent")) {
                "$cleanEndpoint?alt=sse&key=$apiKey"
            } else {
                "$cleanEndpoint/models/$model:streamGenerateContent?alt=sse&key=$apiKey"
            }
            val bodyJson = JSONObject().apply {
                val contents = org.json.JSONArray()
                val userContent = JSONObject().apply {
                    put("role", "user")
                    val parts = org.json.JSONArray().apply {
                        put(JSONObject().put("text", message))
                    }
                    put("parts", parts)
                }
                contents.put(userContent)
                put("contents", contents)
            }
            Request.Builder()
                .url(url)
                .header("Accept", "text/event-stream")
                .header("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()
        } else {
            // Standard OpenAI-compatible format
            val fullUrl = if (cleanEndpoint.endsWith("/chat/completions")) cleanEndpoint else "$cleanEndpoint/chat/completions"
            val messagesArray = org.json.JSONArray()

            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                if (msg.content.isNotBlank()) {
                    val role = if (msg.role == "assistant") "assistant" else "user"
                    messagesArray.put(JSONObject().apply {
                        put("role", role)
                        put("content", msg.content)
                    })
                }
            }
            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })

            val bodyJson = JSONObject().apply {
                put("model", model)
                put("messages", messagesArray)
                put("stream", true)
            }

            Request.Builder()
                .url(fullUrl)
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(bodyJson.toString().toRequestBody(jsonMediaType))
                .build()
        }

        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!call.isCanceled()) {
                    trySend(StreamEvent.Error(e.message ?: "Connection error"))
                    close(e)
                } else {
                    close()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val code = response.code
                    val errorBody = response.body?.string().orEmpty()
                    var msg = "HTTP $code"
                    try {
                        val obj = JSONObject(errorBody)
                        if (obj.has("error")) {
                            val errObj = obj.get("error")
                            msg = if (errObj is JSONObject) errObj.optString("message", msg) else errObj.toString()
                        }
                    } catch (_: Exception) {}
                    trySend(StreamEvent.Error(msg))
                    close()
                    return
                }

                val body = response.body ?: run {
                    trySend(StreamEvent.Error("Empty response"))
                    close()
                    return
                }

                try {
                    val reader = BufferedReader(InputStreamReader(body.byteStream(), Charsets.UTF_8))
                    val fullReply = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        val trimmed = line.trim()
                        if (trimmed.startsWith("data:")) {
                            val data = if (trimmed.startsWith("data: ")) trimmed.substring(6).trim() else trimmed.substring(5).trim()
                            if (data == "[DONE]") {
                                trySend(StreamEvent.Done(reply = fullReply.toString()))
                                break
                            } else if (data.isNotEmpty()) {
                                try {
                                    val json = JSONObject(data)
                                    if (json.has("choices")) {
                                        val choices = json.getJSONArray("choices")
                                        if (choices.length() > 0) {
                                            val firstChoice = choices.getJSONObject(0)
                                            if (firstChoice.has("delta")) {
                                                val delta = firstChoice.getJSONObject("delta")
                                                val content = delta.optString("content", "")
                                                if (content.isNotEmpty()) {
                                                    fullReply.append(content)
                                                    trySend(StreamEvent.Delta(content))
                                                }
                                            }
                                        }
                                    } else if (json.has("candidates")) {
                                        val candidates = json.getJSONArray("candidates")
                                        if (candidates.length() > 0) {
                                            val cand = candidates.getJSONObject(0)
                                            val content = cand.optJSONObject("content")
                                            val parts = content?.optJSONArray("parts")
                                            if (parts != null && parts.length() > 0) {
                                                val text = parts.getJSONObject(0).optString("text", "")
                                                if (text.isNotEmpty()) {
                                                    fullReply.append(text)
                                                    trySend(StreamEvent.Delta(text))
                                                }
                                            }
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                        line = reader.readLine()
                    }
                    trySend(StreamEvent.Done(reply = fullReply.toString()))
                    close()
                } catch (e: Exception) {
                    if (!call.isCanceled()) {
                        trySend(StreamEvent.Error("Stream interrupted"))
                    }
                    close()
                } finally {
                    body.close()
                }
            }
        })

        awaitClose { call.cancel() }
    }
}
