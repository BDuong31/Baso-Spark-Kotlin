package basostudio.basospark.features.chat.chat_room

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
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
import basostudio.basospark.data.model.IMessage
import basostudio.basospark.data.model.User
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    navController: NavController,
    receiverUser: User,
    roomId: String,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState() // Vẫn thu thập danh sách tin nhắn
    var text by remember { mutableStateOf("") }
    val myUser = viewModel.myUser
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        // --- GIAI ĐOẠN 1: KHI VÀO MÀN HÌNH ---
        // Gửi sự kiện 'joinRoom' đến server
        myUser?.id?.let { currentUserId ->
            Log.d("ChatRoomSocket", "Đang gửi sự kiện 'joinRoom' cho phòng: $roomId")
            viewModel.joinRoom(roomId, currentUserId)
        }

        onDispose {
            // --- GIAI ĐOẠN 2: KHI THOÁT KHỎI MÀN HÌNH ---
            // Gửi sự kiện 'leaveRoom' đến server
            myUser?.id?.let { currentUserId ->
                Log.d("ChatRoomSocket", "Đang gửi sự kiện 'leaveRoom' cho phòng: $roomId")
                viewModel.leaveRoom(roomId, currentUserId) // Giả định hàm này có trong ViewModel
            }
        }
    }

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
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        AsyncImage(
                            model = receiverUser.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier.size(56.dp).clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(receiverUser.username)
                    }
                        },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
//                    }
//                }
                actions = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Back")
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
        when (uiState) {
            is MessageUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator() // Hiển thị loading spinner
                }
            }
            is MessageUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text(text = (uiState as MessageUiState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is MessageUiState.Success -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    items(messages) { message ->
                        MessageItem(message = message, isMyMessage = message.user.id == myUser?.id)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: IMessage, isMyMessage: Boolean) {
    val horizontalArrangement = if (isMyMessage) Arrangement.End else Arrangement.Start
    val backgroundColor = if (isMyMessage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically

    ) {
        AsyncImage(
            model = message.user.avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier.size(36.dp).clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = message.content,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}