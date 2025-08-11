package io.github.sd155.aiadvent2025.chat

internal data class ChatViewState(
    val messages: List<ChatMessage> = emptyList(),
)

internal sealed class ChatMessage {
    data class UserMessage(val content: String) : ChatMessage()
    data class AiMessage(val content: String) : ChatMessage()
    data object AiTyping : ChatMessage()
    data object AiError : ChatMessage()
}