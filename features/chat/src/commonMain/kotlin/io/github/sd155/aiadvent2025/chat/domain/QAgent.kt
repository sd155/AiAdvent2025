package io.github.sd155.aiadvent2025.chat.domain

import aiadvent2025.features.chat.generated.resources.Res
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerError
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerSubtask
import io.github.sd155.aiadvent2025.chat.domain.llm.KtorLlmDataSource
import io.github.sd155.aiadvent2025.chat.domain.llm.LlmContextElement
import io.github.sd155.aiadvent2025.chat.domain.llm.LlmContextElementType
import io.github.sd155.aiadvent2025.utils.Result
import io.github.sd155.aiadvent2025.utils.asSuccess
import io.github.sd155.aiadvent2025.utils.next
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal class QAgent {
    private val _llm by lazy { KtorLlmDataSource() }
    private val _json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "result"
    }

    private suspend fun systemPrompt() =
        """
            Role: Task Decomposition Checker.
            
            You receive the task description and the task decomposition as JSON.
            Tou have to check carefully all the subtask tree.
            Valid subtasks should be small, easy, reasonable, one step.
            Your response has to strictly follow the rules:
            1. Valid JSON only, do not wrap it with markers, do not add extra content.
            2. Use 'invalid' type when you find a invalid subtask. Property 'reason' should contain your clear explanation why the subtask is invalid.
            3. Use 'valid' type when all the task tree is valid.
            4. Use the given JSON scheme, do not extend the scheme. The scheme: ${String(Res.readBytes("files/q-agent-response-scheme.json"))}
        """.trimIndent()

    suspend fun request(
        description: String,
        decomposition: List<DecomposerSubtask>
    ): Result<DecomposerError, QResponse> =
        _json.encodeToString<Input>(
            Input(
                description = description,
                decomposition = decomposition
            )
        )
            .let { inputJson ->
                listOf(
                    LlmContextElement(
                        type = LlmContextElementType.System,
                        value = systemPrompt()
                    ),
                    LlmContextElement(
                        type = LlmContextElementType.User,
                        value = inputJson
                    ),
                )
            }
            .let { context -> _llm.postChatCompletions(context) }
            .next { llmElement ->
                _json.decodeFromString<QResponse>(llmElement.value).asSuccess()
            }
}

@Serializable
private data class Input(
    @SerialName("task_description")
    val description: String,
    @SerialName("decomposition")
    val decomposition: List<DecomposerSubtask>,
)

@Serializable
internal sealed class QResponse {
    @Serializable
    @SerialName("valid")
    data class Valid(
        @SerialName("result")
        val result: String,
    ) : QResponse()
    @Serializable
    @SerialName("invalid")
    data class Invalid(
        @SerialName("result")
        val result: String,
        @SerialName("reason")
        val reason: String,
    ) : QResponse()
}
