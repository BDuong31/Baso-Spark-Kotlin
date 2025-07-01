package basostudio.basospark.features.chat.chat_room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.data.model.ChatMessage
import basostudio.basospark.data.model.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    navController: NavController,
    receiverUser: User,
    roomId: String,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    var text by remember { mutableStateOf("") }
    val myUser = viewModel.myUser
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(receiverUser.username) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                placeholder = { Text("Type a message...") },
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(receiverUser.id, text)
                                text = ""
                            }
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(messages) { message ->
                MessageItem(message = message, isMyMessage = message.senderId == myUser?.id)
            }
        }
    }
}

@Composable
fun MessageItem(message: ChatMessage, isMyMessage: Boolean) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    val backgroundColor = if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = horizontalArrangement
    ) {
        Text(
            text = message.content,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}