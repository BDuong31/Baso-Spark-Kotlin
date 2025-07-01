package basostudio.basospark.features.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is ProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ProfileUiState.Success -> {
            ProfileContent(user = state.user, userPosts = state.userPosts, savedPosts = state.savedPosts)
        }
        is ProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}

@Composable
fun ProfileContent(user: User, userPosts: List<Post>, savedPosts: List<Post>) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("My Posts", "Saved")

    Column {
        ProfileHeader(user = user)
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(text = title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> PostGrid(posts = userPosts)
            1 -> PostGrid(posts = savedPosts)
        }
    }
}

@Composable
fun PostGrid(posts: List<Post>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize()
    ) {
        items(posts) { post ->
            Image(
                painter = rememberAsyncImagePainter(model = post.image ?: post.author.avatar),
                contentDescription = "Post",
                modifier = Modifier.aspectRatio(1f).padding(1.dp),
                contentScale = ContentScale.Crop
            )
        }
    }
}
@Composable
fun ProfileHeader(user: User) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = user.avatar,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "@${user.username}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(text = "${user.firstName} ${user.lastName}", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        user.bio?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 16.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStat(value = user.postCount, label = "Posts")
            ProfileStat(value = user.followerCount, label = "Followers")
            // Thêm mục "Following" khi có API
            ProfileStat(value = 0, label = "Following")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
    }
}

@Composable
fun ProfileStat(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}