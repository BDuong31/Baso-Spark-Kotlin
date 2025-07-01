package basostudio.basospark.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.Post
import basostudio.basospark.features.chat.chat_list.ChatListScreen
import basostudio.basospark.features.feed.FeedScreen
import basostudio.basospark.features.notifications.NotificationScreen
import basostudio.basospark.features.post_details.PostDetailsScreen
import basostudio.basospark.features.profile.ProfileScreen
import basostudio.basospark.features.search.SearchScreen
import com.google.gson.Gson
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController) { // Nhận NavController từ AppNavHost
    val navController = rememberNavController()
    val bottomNavItems = listOf(Screen.Feed,Screen.Search,Screen.Notifications , Screen.Profile, Screen.ChatList)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (currentRoute) {
                            Screen.Feed.route -> "Baso Social"
                            Screen.Profile.route -> "Profile"
                            else -> ""
                        }
                    )
                },
                actions = {
                    if (currentRoute == Screen.Profile.route) {
                        IconButton(onClick = { mainNavController.navigate(Screen.Settings.route) }) { // <-- Nút Cài đặt
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = {
                            val sessionManager = SessionManager(navController.context)
                            sessionManager.clearAuthToken()
                            mainNavController.navigate(Screen.Login.route) {
                                popUpTo(Screen.MainFlow.route) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                        label = { Text(screen.label!!) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Feed.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Feed.route) {
                FeedScreen(
                    navController = mainNavController,
                    onPostClick = { post -> // <-- Truyền callback
                        mainNavController.navigate(Screen.PostDetails.createRoute(post.id))
                    }
                )
            } // Dùng mainNavController để điều hướng ra ngoài
            composable(Screen.Profile.route) { ProfileScreen(mainNavController) }
            composable(Screen.ChatList.route) { ChatListScreen(mainNavController) }
            composable(Screen.Search.route) { SearchScreen(mainNavController) }
            composable(Screen.Notifications.route) { NotificationScreen(mainNavController) }

        }
    }
}