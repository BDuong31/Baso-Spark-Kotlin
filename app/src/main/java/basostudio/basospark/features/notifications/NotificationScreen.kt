package basostudio.basospark.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import basostudio.basospark.data.model.Notification

@Composable
fun NotificationScreen(navController: NavController, viewModel: NotificationViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState is NotificationUiState.Loading

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.fetchNotifications() }
    ) {
        when (val state = uiState) {
            is NotificationUiState.Loading -> {
                // Hiển thị khi tải lần đầu, SwipeRefresh sẽ xử lý các lần sau
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is NotificationUiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.notifications) { notification ->
                        NotificationItem(notification = notification)
                    }
                }
            }
            is NotificationUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = notification.sender?.avatar,
                contentDescription = "Sender Avatar",
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
            )
            // Icon cho loại thông báo
            val icon = when (notification.action) {
                "liked" -> Icons.Default.Favorite
                "followed" -> Icons.Default.PersonAdd
                else -> null
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Action type",
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    tint = if (notification.action == "liked") Color.Red else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = notification.sender?.username ?: "Someone",
                fontWeight = FontWeight.Bold
            )
            Text(text = notification.content)
        }
    }
}