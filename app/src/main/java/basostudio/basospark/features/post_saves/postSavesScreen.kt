package basostudio.basospark.features.post_saves

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import basostudio.basospark.R
import basostudio.basospark.data.model.Post
import basostudio.basospark.features.feed.components.PostItem
import coil.compose.AsyncImage

enum class PostSavesFilter { ALL, MEDIA }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostSavesScreen(
    initialFilter: String,
    navController: NavController,
    viewModel: PostSavesViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPostClick: (postId: String) -> Unit,
    onDiscoverClick: () -> Unit
) {
    // Thu thập state từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    val selectedOption = when (initialFilter) {
        "ALL" -> PostSavesFilter.ALL
        "MEDIA" -> PostSavesFilter.MEDIA
        else -> PostSavesFilter.ALL
    }

    Scaffold(
        topBar = {
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize().statusBarsPadding()) {
            when (val state = uiState) {
                is PostSavesUiState.Loading -> {
                    FullScreenLoading()
                }
                is PostSavesUiState.Error -> {
                    FullScreenError(errorMessage = state.message)
                }
                is PostSavesUiState.Success -> {
                    if (state.savedPosts.isEmpty()) {
                        EmptyStateScreen(onDiscoverClick = onDiscoverClick)
                    } else {
                        // 3. Dùng biến `selectedOption` đã được quản lý bằng state
                        when (selectedOption) {
                            PostSavesFilter.ALL -> PostListContentAll(
                                posts = state.savedPosts,
                                onPostClick = onPostClick
                            )
                            PostSavesFilter.MEDIA -> {
                                Log.d("PostSavesScreen", "Rendering media posts")
                                PostListContentMedia(
                                    posts = state.savedPosts,
                                    onPostClick = onPostClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PostListContentAll(
    posts: List<Post>,
    onPostClick: (postId: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = posts, key = { it.id }) { post ->
            // Giả sử PostItem là Composable bạn đã có để hiển thị 1 bài viết đầy đủ
            PostItem(
                post = post,
                onPostClick = { onPostClick(post.id) },
                onLikeClick = { /* Handle like click */ },
                onCommentClick = { /* Handle comment click */ },
                onSaveClick = { /* Handle save click */ },
                onAuthorClick = { /* Handle author click */ },
                onMoreOptionsClick = { /* Handle more options click */ }
            )
        }
    }
}

@Composable
private fun PostListContentMedia(
    posts: List<Post>,
    onPostClick: (postId: String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items = posts, key = { it.id }) { post ->
            Log.d("PostListContentMedia", "Rendering post: ${post.id}")
            val imageUrl = post.image?.replace("localhost", "172.20.10.6") ?: ""
            AsyncImage(
                model = imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Tỷ lệ vuông
                    .clip(MaterialTheme.shapes.medium)
                    .clickable { onPostClick(post.id) },
                contentScale = ContentScale.Crop
            )
        }
    }
}

// ... các Composable phụ trợ khác như EmptyStateScreen, FullScreenLoading,...
// Bạn có thể giữ nguyên chúng.
@Composable
private fun EmptyStateScreen(onDiscoverClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.baso),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Bạn chưa lưu bài viết nào",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onDiscoverClick) {
                Text("Khám phá ngay")
            }
        }
    }
}

@Composable
private fun FullScreenLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenError(errorMessage: String) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = "Đã xảy ra lỗi: $errorMessage")
    }
}