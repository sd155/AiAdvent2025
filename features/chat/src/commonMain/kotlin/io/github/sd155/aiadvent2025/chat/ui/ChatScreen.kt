package io.github.sd155.aiadvent2025.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen() {
    val viewModel: ChatViewModel = viewModel { ChatViewModel() }
    val state by viewModel.state.collectAsState()

    ChatView(
        state = state,
        onPrompt = { viewModel.onViewIntent(ChatViewIntent.UserPrompted(prompt = it)) },
    )
}