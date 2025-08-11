package io.github.sd155.aiadvent2025

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AiAdvent2025",
    ) {
        AppUi()
    }
}