package io.github.sd155.aiadvent2025.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sd155.aiadvent2025.chat.domain.QAgent
import io.github.sd155.aiadvent2025.chat.domain.QResponse
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerAgent
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerResponse
import io.github.sd155.aiadvent2025.utils.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ChatViewModel : ViewModel() {
    private val _agentD = DecomposerAgent()
    private val _agentQ = QAgent()
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
                _agentD.request(intent.prompt)
                    .fold(
                        onSuccess = { responseD ->
                            when (responseD) {
                                is DecomposerResponse.Query -> {
                                    reduceWithMsg(ChatMessage.AiQuery(responseD.question))
                                }
                                is DecomposerResponse.Success -> {
                                    reduceWithMsg(ChatMessage.AiSwitch)
                                    reduceWithMsg(ChatMessage.AiTyping)
                                    _agentQ.request(
                                        description = intent.prompt,
                                        decomposition = responseD.subtasks
                                    )
                                        .fold(
                                            onFailure = { reduceWithMsg(ChatMessage.AiError) },
                                            onSuccess = { responseQ ->
                                                when (responseQ) {
                                                    is QResponse.Invalid ->
                                                        reduceWithMsg(ChatMessage.AiResult(
                                                            description = "Invalid task decomposition!\nReason: ${responseQ.reason}",
                                                            decomposition = responseD.subtasks
                                                        ))
                                                    is QResponse.Valid ->
                                                        reduceWithMsg(ChatMessage.AiResult(
                                                            description = "Task decomposition checked and valid.",
                                                            decomposition = responseD.subtasks
                                                        ))
                                                }
                                            }
                                        )
                                }
                            }
                        },
                        onFailure = { reduceWithMsg(ChatMessage.AiError) }
                    )
            }
        }
    }

    private fun reduceWithMsg(aiMsg: ChatMessage) {
        if (_state.value.messages.last() is ChatMessage.AiTyping)
            _state.value.reduce { copy(messages = messages - messages.last()) }
        _state.value.reduce { copy(messages = messages + aiMsg) }
    }

    private fun ChatViewState.reduce(reducer: ChatViewState.() -> ChatViewState): ChatViewState {
        _state.value = reducer(this)
        return this
    }
}
