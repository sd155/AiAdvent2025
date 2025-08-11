package io.github.sd155.aiadvent2025

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import io.github.sd155.aiadvent2025.chat.ChatScreen

@Composable
internal fun AppUi() {
    MaterialTheme {
        ChatScreen()
    }
}