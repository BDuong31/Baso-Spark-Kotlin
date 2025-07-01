package basostudio.basospark.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.User
import basostudio.basospark.features.auth.login.LoginScreen
import basostudio.basospark.features.auth.register.RegisterScreen
import basostudio.basospark.features.chat.chat_room.ChatRoomScreen
import basostudio.basospark.features.create_post.CreatePostScreen
import basostudio.basospark.features.post_details.PostDetailsScreen
import basostudio.basospark.features.profile.other_profile.OtherProfileScreen
import com.google.gson.Gson
import basostudio.basospark.features.settings.SettingsScreen

@Composable
fun AppNavHost(paddingValues: PaddingValues) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val startDestination = if (sessionManager.fetchAuthToken() != null) Screen.MainFlow.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // Luồng chính sau khi đăng nhập
        composable(Screen.MainFlow.route) { MainScreen(mainNavController = navController) }

        // Các màn hình chi tiết
        composable(Screen.CreatePost.route) { CreatePostScreen(navController) }

        composable(
            route = Screen.PostDetails.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            PostDetailsScreen(navController)
        }

        composable(
            route = Screen.OtherProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            OtherProfileScreen(navController)
        }

        composable(
            route = Screen.ChatRoom.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userJson = navController.previousBackStackEntry?.savedStateHandle?.get<String>("user")
            val receiverUser = Gson().fromJson(userJson, User::class.java)
            val roomId = backStackEntry.arguments?.getString("roomId")

            if (receiverUser != null && roomId != null) {
                ChatRoomScreen(navController, receiverUser, roomId)
            }
        }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
    }
}