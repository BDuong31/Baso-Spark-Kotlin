package basostudio.basospark.features.profile

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import basostudio.basospark.R
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.features.feed.components.PostItem
import basostudio.basospark.ui.navigation.Screen
import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- LOGIC NHẬN TÍN HIỆU ---
    // Lắng nghe xem có tín hiệu "profile_updated" được gửi về không
    val profileUpdateResult = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("profile_updated")?.observeAsState()

    // Khi nhận được tín hiệu (giá trị là true), ta sẽ hành động
    LaunchedEffect(profileUpdateResult?.value) {
        if (profileUpdateResult?.value == true) {
            Log.d("ProfileScreen", "Nhận được tín hiệu profile_updated, đang tải lại dữ liệu...")
            viewModel.refreshData()
            navController.currentBackStackEntry?.savedStateHandle?.set("profile_updated", false)
        }
    }
    // --- KẾT THÚC LOGIC NHẬN TÍN HIỆU ---

    // Giao diện giữ nguyên, nó sẽ tự cập nhật khi uiState thay đổi
    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Success -> {
            ProfileContent(
                navController = navController,
                user = state.user,
                userPosts = state.userPosts,
                savedPosts = state.savedPosts
            )
        }
        is ProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileContent(
    navController: NavController,
    user: User,
    userPosts: List<Post>,
    savedPosts: List<Post>
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Bài viết", "Đã lưu")

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            ProfileHeader(
                user = user,
                onNavigateBack = { navController.popBackStack() },
                navController = navController
            )
        }

        stickyHeader {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        val postsToShow = when (selectedTabIndex) {
            0 -> userPosts
            1 -> savedPosts
            else -> emptyList()
        }

        if (postsToShow.isEmpty()) {
            item {
                EmptyContent()
            }
        } else {
            items(items = postsToShow, key = { it.id }) { post ->
                PostItem(
                    post = post,
                    onPostClick = { /* TODO */ },
                    onLikeClick = { /* TODO */ },
                    onCommentClick = { /* TODO */ },
                    onSaveClick = { /* TODO */ },
                    onAuthorClick = { /* TODO */ },
                    onMoreOptionsClick = { /* TODO */ },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileHeader(user: User, onNavigateBack: () -> Unit, navController: NavController) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            val coverUrl = user.cover?.replace("localhost", "192.168.1.139") ?: user.cover
            AsyncImage(
                model = coverUrl ?: R.drawable.defaulcover,
                contentDescription = "${user.username}'s avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            )

            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            val AvatarUrl = user.avatar?.replace("localhost", "172.20.10.2") ?: user.avatar
            AsyncImage(
                model = AvatarUrl ?: R.drawable.defaultavatar,
                contentDescription = "${user.username}'s avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp)
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${user.firstName} ${user.lastName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "@${user.username}", color = Color.Gray)
            }
            IconButton(onClick = { /*TODO*/ }) {
                Icon(Icons.Default.Share, contentDescription = "Share")
            }
            IconButton(onClick = {
                navController.navigate(Screen.EditProfile.route)
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
            user.bio?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(16.dp))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                ProfileStat(value = user.postCount, label = "Bài viết")
                ProfileStat(value = user.followerCount, label = "Người theo dõi")
                ProfileStat(value = 0, label = "Đang theo dõi") // Thay bằng dữ liệu thật
            }
        }
    }
}

@Composable
fun ProfileStat(value: Int, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun EmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Chưa có bài viết nào", color = Color.Gray)
    }
}