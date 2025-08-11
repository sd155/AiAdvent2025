package io.github.sd155.aiadvent2025.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sd155.aiadvent2025.utils.Result
import io.github.sd155.aiadvent2025.utils.asFailure
import io.github.sd155.aiadvent2025.utils.asSuccess
import io.github.sd155.aiadvent2025.utils.fold
import io.github.sd155.aiadvent2025.utils.next
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class ChatViewModel : ViewModel() {
    private val _ai by lazy { Ai() }
    private val _state = MutableStateFlow(ChatViewState())
    internal val state: StateFlow<ChatViewState> = _state.asStateFlow()

    internal fun onViewIntent(intent: ChatViewIntent) = viewModelScope.launch(Dispatchers.Default) {
        when (intent) {
            is ChatViewIntent.UserPrompted -> {
                _state.value.reduce {
                    val userMsg = ChatMessage.UserMessage(intent.prompt)
                    copy(messages = messages + userMsg)
                }
                _state.value.reduce { copy(messages = messages + ChatMessage.AiTyping) }
                _ai.request(intent.prompt)
                    .fold(
                        onSuccess = { response -> ChatMessage.AiMessage(response) },
                        onFailure = { ChatMessage.AiError }
                    )
                    .also { aiMsg ->
                        if (_state.value.messages.last() is ChatMessage.AiTyping)
                            _state.value.reduce { copy(messages = messages - messages.last()) }
                        _state.value.reduce { copy(messages = messages + aiMsg) }
                    }
            }
        }
    }

    private fun ChatViewState.reduce(reducer: ChatViewState.() -> ChatViewState): ChatViewState {
        _state.value = reducer(this)
        return this
    }
}


private class Ai {
    private val _context by lazy { mutableListOf<MessageDto>() }
    private val _httpClient by lazy {
        HttpClient(CIO) {
            install(Logging) {
                level = LogLevel.ALL
            }

            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }

            install(HttpTimeout) {
                connectTimeoutMillis = 15000
                requestTimeoutMillis = 30000
            }

            val baseUrl = "https://openrouter.ai/api/v1/chat/completions"
            val apiKey = "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"
            defaultRequest {
                url(baseUrl)
                header("Content-Type", "application/json; charset=UTF-8")
                header("Authorization", "Bearer $apiKey")
            }
        }
    }

    suspend fun request(prompt: String): Result<AiError, String> {
        val userMessage = MessageDto(
            role = "user",
            content = prompt,
        )
        _context.add(userMessage)
        val payload = RequestDto(
            model = "qwen/qwen3-235b-a22b:free",
            messages = _context,
        )
        return try {
            _httpClient
                .post { setBody(payload) }
                .let { response ->
                    when (response.status) {
                        HttpStatusCode.OK ->
                            response.body<ResponseDto>().choices?.first()?.message?.asSuccess()
                                ?: AiError.asFailure()
                        else -> AiError.asFailure()
                    }
                }
                .next { aiMessage ->
                    _context.add(aiMessage)
                    aiMessage.content?.asSuccess() ?: AiError.asFailure()
                }
        }
        catch (e: Exception) {
            AiError.asFailure()
        }
    }
}

private data object AiError

@Serializable
private data class RequestDto(
    @SerialName("model")
    val model: String,
    @SerialName("messages")
    val messages: List<MessageDto>,
)

@Serializable
private data class MessageDto(
    @SerialName("role")
    val role: String?,
    @SerialName("content")
    val content: String?,
)

@Serializable
private data class ResponseDto(
    @SerialName("choices")
    val choices: List<ChoiceDto>?,
)

@Serializable
private data class ChoiceDto(
    @SerialName("message")
    val message: MessageDto?,
)