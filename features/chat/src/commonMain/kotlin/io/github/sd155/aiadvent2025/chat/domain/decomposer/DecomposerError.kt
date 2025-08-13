package io.github.sd155.aiadvent2025.chat.domain.decomposer

internal sealed class DecomposerError {
    data object LlmNetworkError : DecomposerError()
}