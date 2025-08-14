package io.github.sd155.aiadvent2025.chat.ui

import aiadvent2025.features.chat.generated.resources.Res
import aiadvent2025.features.chat.generated.resources.ai_failed
import aiadvent2025.features.chat.generated.resources.prompt_hint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerResponse
import io.github.sd155.aiadvent2025.chat.domain.decomposer.DecomposerSubtask
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ChatView(
    state: ChatViewState,
    onPrompt: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .safeContentPadding()
            .fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        MessageList(
            modifier = Modifier.weight(1f),
            messages = state.messages,
        )
        ChatPrompt(
            onPrompt = onPrompt
        )
    }
}

@Composable
private fun MessageList(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Yellow)
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        itemsIndexed(messages) { index, message ->
            when (message) {
                is ChatMessage.AiResult -> RemoteBubble(
                    status = "result",
                    msg = message.description,
                    decomposition = message.decomposition
                )
                ChatMessage.AiTyping -> RemoteLoading()
                is ChatMessage.UserMessage -> LocalBubble(message.content)
                ChatMessage.AiError -> RemoteError()
                is ChatMessage.AiQuery -> RemoteBubble(
                    status = "query",
                    msg = message.question
                )
                ChatMessage.AiSwitch -> RemoteBubble(
                    status = "agent switch",
                    msg = "Decomposer agent done, switching to Q agent.."
                )
            }
            if (index < messages.size - 1)
                Spacer(modifier = Modifier.height(8.dp))
        }
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(
                    index = messages.size - 1,
                )
            }
        }
    }
}

@Composable
private fun LocalBubble(content: String) =
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .background(
                    color = Color.Green,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .padding(16.dp)
                .weight(2f),
        ) {
            Text(text = content)
        }
    }

@Composable
private fun RemoteBubble(
    status: String,
    msg: String,
    decomposition: List<DecomposerSubtask>? = null,
) =
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Column (
            modifier = Modifier
                .background(
                    color = Color.Cyan,
                    shape = RoundedCornerShape(size = 16.dp)
                )
                .padding(8.dp)
                .weight(2f),
        ) {
            Text(text = "Status: $status")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = msg)
            decomposition?.let {
                decomposition.forEach { subtask ->
                    SubtaskItem(subtask = subtask, depth = 0)
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
    }

@Composable
private fun SubtaskItem(subtask: DecomposerSubtask, depth: Int) {
    val indent = (depth * 8).dp
    Row {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${subtask.id} ${subtask.name}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = indent)
            )
            Text(
                text = subtask.instruction,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = indent + 16.dp, bottom = 8.dp)
            )
            subtask.subtasks.forEach { child ->
                SubtaskItem(subtask = child, depth = depth + 1)
            }
        }
    }
}

@Composable
private fun RemoteLoading() =
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(32.dp),
            strokeWidth = 16.dp,
        )
    }

@Composable
private fun RemoteError() =
    Row(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        Box(
            modifier = Modifier
                .background(color = Color.Red)
                .padding(16.dp)
                .weight(2f),
        ) {
            Text(text = stringResource(Res.string.ai_failed))
        }
        Spacer(modifier = Modifier.weight(1f))
    }

@Composable
private fun ChatPrompt(
    onPrompt: (String) -> Unit,
) {
    var prompt by rememberSaveable { mutableStateOf("") }
    TextField(
        modifier = Modifier
            .fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(text = stringResource(Res.string.prompt_hint)) },
        value = prompt,
        onValueChange = { prompt = it },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(
            onSend = {
                onPrompt(prompt)
                prompt = ""
            }
        )
    )
}