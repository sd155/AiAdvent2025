package io.github.sd155.aiadvent2025.chat.domain.decomposer

import aiadvent2025.features.chat.generated.resources.Res
import io.github.sd155.aiadvent2025.chat.domain.llm.KtorLlmDataSource
import io.github.sd155.aiadvent2025.chat.domain.llm.LlmContextElement
import io.github.sd155.aiadvent2025.chat.domain.llm.LlmContextElementType
import io.github.sd155.aiadvent2025.utils.Result
import io.github.sd155.aiadvent2025.utils.asSuccess
import io.github.sd155.aiadvent2025.utils.next
import kotlinx.serialization.json.Json

internal class DecomposerAgent() {
    private val _llm by lazy { KtorLlmDataSource() }
    private val _context by lazy { mutableListOf<LlmContextElement>() }
    private val _json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "result"
    }

    private suspend fun systemPrompt() =
        """
            Role: Task Decomposer.
            
            You should take first user prompt as a task description, which you have to decompose to small, easy, one step subtasks.
            Communicate with the user to gather task details and context.
            Your response has to strictly follow the rules:
            1. Valid JSON only, do not wrap it with markers, do not add extra content.
            2. Use 'query' type when you need to ask user for details. Property 'question' should contain your question only.
            3. Use 'success' type when the task decomposition is ready, you have all the subtasks, you have no questions to resolve the decomposition.
            4. Use the given JSON scheme, do not extend the scheme. The scheme: ${String(Res.readBytes("files/decomposer-agent-response-scheme.json"))}
        """.trimIndent()

    suspend fun request(prompt: String): Result<DecomposerError, DecomposerResponse> =
        LlmContextElement(
            type = LlmContextElementType.User,
            value = prompt,
        )
            .let { userElement ->
                if (_context.isEmpty()) {
                    _context.add(
                        LlmContextElement(
                        type = LlmContextElementType.System,
                        value = systemPrompt()
                    )
                    )
                }

                _context.add(userElement)
                _llm.postChatCompletions(_context)
                    .next { llmElement ->
                        _context.add(llmElement)
                        _json.decodeFromString<DecomposerResponse>(llmElement.value).asSuccess()

                    }
            }
}