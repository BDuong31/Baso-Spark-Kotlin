package basostudio.basospark.ui.navigation

import basostudio.basospark.ui.navigation.AppDrawerContent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.util.SnackbarManager
import basostudio.basospark.features.chat.chat_list.ChatListScreen
import basostudio.basospark.features.feed.FeedScreen
import basostudio.basospark.features.notifications.NotificationScreen
import basostudio.basospark.features.profile.ProfileScreen
import basostudio.basospark.features.search.SearchScreen
import kotlinx.coroutines.launch
import basostudio.basospark.R
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBarDefaults.windowInsets
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import basostudio.basospark.features.post_saves.PostSavesFilter
import basostudio.basospark.features.post_saves.PostSavesScreen
import basostudio.basospark.features.post_saves.PostSavesViewModel
import androidx.navigation.navArgument
import androidx.navigation.NavType
import basostudio.basospark.MainViewModel
import basostudio.basospark.features.explore.ExploreScreen
import coil.compose.AsyncImage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainNavController: NavController, viewModel: MainViewModel = hiltViewModel()) {
    // 1. THÊM STATE ĐỂ QUẢN LÝ DRAWER VÀ COROUTINE SCOPE
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()

    val bottomNavController = rememberNavController() // Controller cho các tab dưới cùng
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by SnackbarManager.messages.collectAsState(initial = null)
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            launch {
                snackbarHostState.showSnackbar(it)
                SnackbarManager.clearMessage()
            }
        }
    }
    val bottomNavItems = listOf(
        Screen.Feed,
        Screen.Notifications,
        Screen.ChatList,
        Screen.PostSaves,
        Screen.Profile,
        Screen.Explore
    )
    var selectedOption by remember { mutableStateOf(PostSavesFilter.ALL) }
    val sessionManager = SessionManager(mainNavController.context)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentUser = currentUser,
                onCloseDrawer = { scope.launch { drawerState.close() } },
                onNavigate = { route, isBottomNav ->
                    scope.launch { drawerState.close() }
                    // FIX: Dùng đúng NavController cho từng loại điều hướng
                    if (isBottomNav) {
                        bottomNavController.navigate(route) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    } else {
                        mainNavController.navigate(route)
                    }
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    viewModel.logout()
                    mainNavController.navigate(Screen.Login.route) {
                        popUpTo(Screen.MainFlow.route) { inclusive = true }
                    }
                }
            )
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = if (currentRoute == Screen.Feed.route) {
                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                } else {
                    Modifier
                },
                contentWindowInsets = WindowInsets(0.dp),
                topBar = {
                    if (currentRoute != Screen.Profile.route) {
                        TopAppBar(
                            title = {
                                when (currentRoute) {
                                    Screen.PostSaves.route -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { bottomNavController.popBackStack() },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Đóng tìm kiếm"
                                                )
                                            }
                                            FilterChip(
                                                selected = selectedOption == PostSavesFilter.ALL,
                                                onClick = { selectedOption = PostSavesFilter.ALL },
                                                label = { Text("Tất cả") }
                                            )
                                            FilterChip(
                                                selected = selectedOption == PostSavesFilter.MEDIA,
                                                onClick = {
                                                    selectedOption = PostSavesFilter.MEDIA
                                                }, // Cập nhật state
                                                label = { Text("Có hình ảnh") }
                                            )
                                        }
                                    }

                                    Screen.Notifications.route -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = { bottomNavController.popBackStack() },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Đóng tìm kiếm"
                                                )
                                            }
                                            Text("Notification")
                                        }
                                    }

                                    Screen.Feed.route -> {
                                        Image(
                                            painter = painterResource(id = R.drawable.baso),
                                            contentDescription = "App Logo",
                                            modifier = Modifier.height(50.dp),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    Screen.Search.route -> Text("Search")
                                    Screen.ChatList.route -> Text("Chats")
                                    Screen.Explore.route -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth(),

                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { bottomNavController.popBackStack() },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ArrowBack,
                                                    contentDescription = "Đóng Khám phá"
                                                )
                                            }
                                        }
                                    }

                                }
                            },
                            actions = {
                                if (currentRoute == Screen.Notifications.route) {
                                    Button(
                                        onClick = {}
                                    ) {
                                        Text("read all")
                                    }
                                }
                                if (currentRoute == Screen.Feed.route) {
                                    val user = sessionManager.fetchUserDetails()
                                    IconButton(onClick = { mainNavController.navigate(Screen.Search.route) }) {
                                        Icon(Icons.Default.Search, contentDescription = "Search")
                                    }
                                    IconButton(onClick = { mainNavController.navigate(Screen.CreatePost.route) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Create Post")
                                    }
                                    val avatarUrl = user?.avatar?.replace("localhost", "172.20.10.2")

                                    // 4. THÊM MODIFIER.CLICKABLE VÀO ASYNCIMAGE
                                    AsyncImage(
                                        model = avatarUrl ?: R.drawable.defaultavatar,
                                        contentDescription = "$user.username's avatar",
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                scope.launch {
                                                    drawerState.open() // Mở drawer khi click
                                                }
                                            }
                                    )
                                }
                                if (currentRoute == Screen.Profile.route) {
                                    IconButton(onClick = { mainNavController.navigate(Screen.Settings.route) }) {
                                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                                    }
                                    IconButton(onClick = {
                                        val sessionManager = SessionManager(bottomNavController.context)
                                        sessionManager.clearSession()
                                        mainNavController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.MainFlow.route) { inclusive = true }
                                        }
                                    }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ExitToApp,
                                            contentDescription = "Logout"
                                        )
                                    }
                                }
                            },
                            scrollBehavior = if (currentRoute == Screen.Feed.route) scrollBehavior else null,
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                scrolledContainerColor = MaterialTheme.colorScheme.surface
                            ),
                        )
                    }
                },
                bottomBar = { /* Để trống */ }
            ) { innerPadding ->
                NavHost(
                    bottomNavController,
                    startDestination = Screen.Feed.route,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    composable(Screen.Feed.route) {
                        FeedScreen(
                            navController = mainNavController,
                            innerPadding = innerPadding,
                            onPostClick = { post ->
                                mainNavController.navigate(Screen.PostDetails.createRoute(post.id))
                            }
                        )
                    }
                    composable(Screen.Profile.route) { ProfileScreen(mainNavController) }
                    composable(Screen.ChatList.route) { ChatListScreen(mainNavController) }
                    composable(
                        route = Screen.PostSaves.route,
                        arguments = listOf(navArgument("filter") {
                            type = NavType.StringType
                            defaultValue = "ALL"
                        })
                    ) { backStackEntry ->
                        val initialFilter = ""
                        Log.d(
                            "PostSavesScreen",
                            "Rendering PostSavesScreen with filter: $selectedOption"
                        )
                        PostSavesScreen(
                            initialFilter = selectedOption.toString(),
                            navController = mainNavController,
                            onNavigateBack = { mainNavController.popBackStack() },
                            onPostClick = { postId ->
                                mainNavController.navigate(Screen.PostDetails.createRoute(postId))
                            },
                            onDiscoverClick = {
                                mainNavController.navigate(Screen.Feed.route)
                            }
                        )
                    }
                    composable(Screen.Search.route) { SearchScreen(mainNavController) }
                    composable(Screen.Notifications.route) { NotificationScreen(mainNavController) }
                    composable(Screen.Explore.route) { ExploreScreen(mainNavController) }
                }
            }

            NavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 0.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(24.dp)),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val currentDestination = navBackStackEntry?.destination
                bottomNavItems.forEach { screen ->
                    val isSelected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = { Icon(screen.icon!!, contentDescription = null) },
                        label = null,
                        selected = isSelected,
                        onClick = {
                            if (screen.route == Screen.CreatePost.route) {
                                mainNavController.navigate(Screen.CreatePost.route)
                            } else {
                                bottomNavController.navigate(screen.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }
}

