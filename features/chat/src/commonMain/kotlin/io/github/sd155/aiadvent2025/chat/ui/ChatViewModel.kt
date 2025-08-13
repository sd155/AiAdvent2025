package io.github.sd155.aiadvent2025.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerAgent
import io.github.sd155.aiadvent2025.utils.fold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ChatViewModel : ViewModel() {
    private val _agent = DecomposerAgent()
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
                _agent.request(intent.prompt)
                    .fold(
                        onSuccess = { response -> ChatMessage.AiMessage(response) },
                        onFailure = { ChatMessage.AiError }
                    )
                    .also { aiMsg ->
                        if (_state.value.messages.last() is ChatMessage.AiTyping)
                            _state.value.reduce { copy(messages = messages - messages.last()) }
                        _state.value.reduce { copy(messages = messages + aiMsg) }
                    }
            }
        }
    }

    private fun ChatViewState.reduce(reducer: ChatViewState.() -> ChatViewState): ChatViewState {
        _state.value = reducer(this)
        return this
    }
}



