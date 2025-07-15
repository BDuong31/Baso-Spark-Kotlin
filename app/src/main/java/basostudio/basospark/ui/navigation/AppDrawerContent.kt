// File: app/src/main/java/basostudio/basospark/ui/navigation/AppDrawerContent.kt
package basostudio.basospark.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import basostudio.basospark.data.model.User

@Composable
fun AppDrawerContent(
    mainNavController: NavController, // Chỉ sử dụng NavController cấp cao nhất
    currentUser: User?,
    onCloseDrawer: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            currentUser?.let {
                Row(
                    modifier = Modifier.padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = it.avatar,
                        contentDescription = "User Avatar",
                        modifier = Modifier.size(60.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(it.username, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("@${it.username}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            val menuItems = listOf(
                Screen.Profile,
                Screen.Notifications,
                Screen.Settings
            )
            menuItems.forEach { screen ->
                NavigationDrawerItem(
                    icon = { screen.icon?.let { Icon(it, contentDescription = screen.label) } },
                    label = { screen.label?.let { Text(it) } },
                    selected = false,
                    onClick = {
                        onCloseDrawer()
                        // **SỬA LỖI:** LUÔN DÙNG mainNavController để điều hướng
                        if (mainNavController.currentDestination?.route != screen.route) {
                            mainNavController.navigate(screen.route)
                        }
                    },
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    onCloseDrawer()
                    mainNavController.navigate(Screen.CreatePost.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Post")
            }
        }
    }
}