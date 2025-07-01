package basostudio.basospark.features.chat.chat_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.gson.Gson
import basostudio.basospark.data.model.ChatRoom
import basostudio.basospark.ui.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ChatListScreen(navController: NavController, viewModel: ChatListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when(val state = uiState) {
        is ChatListUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ChatListUiState.Success -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.chatRooms) { chatRoom ->
                    ChatRoomItem(chatRoom = chatRoom, onClick = {
                        val userJson = Gson().toJson(chatRoom.messager)
                        navController.currentBackStackEntry?.savedStateHandle?.set("user", userJson)
                        navController.navigate(Screen.ChatRoom.createRoute(chatRoom.id))
                    })
                }
            }
        }
        is ChatListUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message)
            }
        }
    }
}

@Composable
fun ChatRoomItem(chatRoom: ChatRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = chatRoom.messager.avatar,
            contentDescription = "Avatar",
            modifier = Modifier.size(56.dp).clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = chatRoom.messager.username, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chatRoom.messages?.content ?: "No messages yet",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}