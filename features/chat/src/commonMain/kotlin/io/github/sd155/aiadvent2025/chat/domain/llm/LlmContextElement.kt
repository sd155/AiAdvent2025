package io.github.sd155.aiadvent2025.chat.domain.llm

internal data class LlmContextElement(
    val type: LlmContextElementType,
    val value: String,
)

internal enum class LlmContextElementType { System, User, Llm }