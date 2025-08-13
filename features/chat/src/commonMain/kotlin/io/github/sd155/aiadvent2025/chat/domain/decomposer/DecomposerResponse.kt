package io.github.sd155.aiadvent2025.chat.domain.decomposer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class DecomposerResponse {
    @Serializable
    @SerialName("success")
    data class Success(
        @SerialName("result")
        val result: String,
        @SerialName("subtasks")
        val subtasks: List<DecomposerSubtask>,
    ) : DecomposerResponse()
    @Serializable
    @SerialName("query")
    data class Query(
        @SerialName("result")
        val result: String,
        @SerialName("question")
        val question: String,
    ) : DecomposerResponse()
}

@Serializable
internal data class DecomposerSubtask(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("instruction")
    val instruction: String,
    @SerialName("subtasks")
    val subtasks: List<DecomposerSubtask> = emptyList()
)