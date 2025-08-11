package io.github.sd155.aiadvent2025.chat

internal sealed class ChatViewIntent {
    data class UserPrompted(
        val prompt: String,
    ) : ChatViewIntent()
}