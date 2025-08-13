package io.github.sd155.aiadvent2025.chat.ui

internal sealed class ChatViewIntent {
    data class UserPrompted(
        val prompt: String,
    ) : ChatViewIntent()
}