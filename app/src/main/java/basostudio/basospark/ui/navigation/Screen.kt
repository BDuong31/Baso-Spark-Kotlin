package basostudio.basospark.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login_screen")
    object Register : Screen("register_screen")
    object CreatePost : Screen("create_post_screen")
    object PostDetails : Screen("post_details_screen/{postId}") {
        fun createRoute(postId: String) = "post_details_screen/$postId"
    }
    object MainFlow : Screen("main_flow")
    object Feed : Screen("feed_screen", "Feed", Icons.Default.Home)
    object Profile : Screen("profile_screen?userId={userId}", "Profile", Icons.Default.Person) {
        fun createRoute(userId: String) = "profile_screen?userId=$userId"
    }
    object ChatList : Screen("chat_list_screen", "Chat", Icons.Default.Chat)
    object ChatRoom : Screen("chat_room_screen/{roomId}") {
        fun createRoute(roomId: String) = "chat_room_screen/$roomId"
    }
    object OtherProfile : Screen("other_profile_screen/{userId}") {
        fun createRoute(userId: String) = "other_profile_screen/$userId"
    }
    object Search : Screen("search_screen", "Search", Icons.Default.Search)
    object Notifications : Screen("notifications_screen", "Activity", Icons.Default.Notifications)
    object Settings : Screen("settings_screen")

}