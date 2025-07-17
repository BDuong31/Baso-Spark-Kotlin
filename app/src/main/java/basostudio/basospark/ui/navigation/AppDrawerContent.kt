package basostudio.basospark.ui.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import basostudio.basospark.R
import basostudio.basospark.data.model.User
import coil.compose.AsyncImage

/**
 * Composable này định nghĩa nội dung cho thanh điều hướng (Navigation Drawer).
 */
@Composable
fun AppDrawerContent(
    currentUser: User?,
    onNavigate: (route: String, isBottomNav: Boolean) -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baso),
                contentDescription = "App Logo",
                modifier = Modifier.height(50.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(16.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
                label = { Text("Trang chủ") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.Feed.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") },
                label = { Text("Thông báo") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.Notifications.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Chat, contentDescription = "Tin nhắn") },
                label = { Text("Tin nhắn") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.ChatRoom.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Bookmarks, contentDescription = "Đã lưu") },
                label = { Text("Đã lưu") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.PostSaves.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Trang cá nhân") },
                label = { Text("Trang cá nhân") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.Profile.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Explore, contentDescription = "Khám Phá") },
                label = { Text("Khám phá") },
                selected = false,
                onClick = { onCloseDrawer(); onNavigate(Screen.Explore.route, true) },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.weight(1f))

            currentUser?.let { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .padding(16.dp), // Thêm padding cho cả Row
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = user.avatar ?: R.drawable.defaultavatar,
                        contentDescription = "User Avatar",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${user.firstName} ${user.lastName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis // Thêm dấu ... nếu tên quá dài
                        )
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { onCloseDrawer(); onNavigate(Screen.Settings.route, false) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Cài đặt")
                    }
                    IconButton(onClick = { onCloseDrawer(); onLogout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Đăng xuất")
                    }
                }
            }
        }
    }
}