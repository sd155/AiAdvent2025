package io.github.sd155.aiadvent2025.chat.ui

import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerSubtask

internal data class ChatViewState(
    val messages: List<ChatMessage> = emptyList(),
)

internal sealed class ChatMessage {
    data class UserMessage(val content: String) : ChatMessage()
    data class AiQuery(val question: String): ChatMessage()
    data object AiSwitch : ChatMessage()
    data class AiResult(
        val description: String,
        val decomposition: List<DecomposerSubtask>) : ChatMessage()
    data object AiTyping : ChatMessage()
    data object AiError : ChatMessage()
}