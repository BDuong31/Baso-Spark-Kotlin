package basostudio.basospark.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import basostudio.basospark.data.model.Notification
import basostudio.basospark.R
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    navController: NavController,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState by viewModel.filteredNotifications.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).statusBarsPadding()) {
            // Thanh Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == NotificationFilter.ALL,
                    onClick = { viewModel.onFilterSelected(NotificationFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedFilter == NotificationFilter.LIKES,
                    onClick = { viewModel.onFilterSelected(NotificationFilter.LIKES) },
                    label = { Text("Likes") }
                )
                FilterChip(
                    selected = selectedFilter == NotificationFilter.FOLLOWS,
                    onClick = { viewModel.onFilterSelected(NotificationFilter.FOLLOWS) },
                    label = { Text("Follows") }
                )
                FilterChip(
                    selected = selectedFilter == NotificationFilter.REPLIES,
                    onClick = { viewModel.onFilterSelected(NotificationFilter.REPLIES) },
                    label = { Text("Replies") }
                )
            }

            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = isLoading),
                onRefresh = { viewModel.fetchNotifications() }
            ) {
                when (val state = uiState) {
                    is NotificationUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is NotificationUiState.Success -> {
                        if (state.notifications.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(if (selectedFilter == NotificationFilter.ALL) "You have no notifications yet." else "No notifications in this category.")
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(state.notifications, key = { it.id }) { notification ->
                                    NotificationItem(notification = notification)
                                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                                }
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
    }
}

@Composable
fun NotificationItem(notification: Notification) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            notification.sender?.avatar?.replace("localhost", "172.20.10.6")
            AsyncImage(
                model = notification.sender?.avatar ?: R.drawable.defaultavatar,
                contentDescription = "Sender Avatar",
                modifier = Modifier.size(50.dp).clip(CircleShape)
            )
            val (icon, iconColor) = when (notification.action) {
                "liked" -> Icons.Default.Favorite to Color(0xFFE0245E)
                "followed" -> Icons.Default.PersonAdd to MaterialTheme.colorScheme.primary
                "replied" -> Icons.AutoMirrored.Filled.Chat to Color(0xFF17BF63)
                else -> null to Color.Gray
            }
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Action type",
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(4.dp),
                    tint = iconColor
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(notification.sender?.username ?: "Someone")
                }
                append(" ${notification.content}")
            }
            Text(text = annotatedText, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "4 months ago", // TODO: Implement relative time formatting
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}