package basostudio.basospark.features.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import basostudio.basospark.data.model.Post
import basostudio.basospark.features.feed.components.PostItem
import basostudio.basospark.ui.navigation.Screen
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    navController: NavController,
    viewModel: FeedViewModel = hiltViewModel(),
    onPostClick: (Post) -> Unit
) {
    // Lấy dữ liệu phân trang từ ViewModel
    val lazyPagingItems: LazyPagingItems<Post> = viewModel.postsStream.collectAsLazyPagingItems()
    // Lắng nghe trạng thái cập nhật của từng bài đăng (like/save)
    val updatedPostsState by viewModel.updatedPostsState.collectAsState()

    // Xác định trạng thái "đang làm mới" cho SwipeRefresh
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Baso Social") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate(Screen.CreatePost.route)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { paddingValues ->
        // SwipeRefresh để người dùng có thể kéo xuống làm mới
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Hiển thị danh sách các bài đăng
                items(
                    count = lazyPagingItems.itemCount,
                    // Cung cấp key giúp Compose nhận diện và tối ưu hóa item
                    key = { index -> lazyPagingItems.peek(index)?.id ?: "" }
                ) { index ->
                    val post = lazyPagingItems[index]
                    if (post != null) {
                        // Kiểm tra xem có trạng thái cập nhật cho bài đăng này không
                        val updatedState = updatedPostsState[post.id]
                        // Ưu tiên hiển thị trạng thái mới nhất từ updatedState
                        val postToShow = if (updatedState != null) {
                            post.copy(
                                hasLiked = updatedState.hasLiked ?: post.hasLiked,
                                likedCount = updatedState.likedCount ?: post.likedCount,
                                hasSaved = updatedState.hasSaved ?: post.hasSaved
                            )
                        } else {
                            post
                        }

                        PostItem(
                            post = postToShow,
                            onPostClick = { onPostClick(postToShow) },
                            onLikeClick = {
                                // Gọi hàm trong ViewModel để xử lý like/unlike
                                viewModel.toggleLikeStatus(postToShow)
                            },
                            onSaveClick = {
                                // Gọi hàm trong ViewModel để xử lý save/unsave
                                viewModel.toggleSaveStatus(postToShow)
                            },
                            onAuthorClick = { authorId ->
                                navController.navigate(Screen.OtherProfile.createRoute(authorId))
                            }
                        )
                    }
                }

                // Xử lý các trạng thái của Paging (loading, error)
                lazyPagingItems.loadState.apply {
                    when {
                        // --- Trạng thái làm mới (tải lần đầu) ---
                        refresh is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        refresh is LoadState.Error -> {
                            val e = lazyPagingItems.loadState.refresh as LoadState.Error
                            item {
                                ErrorState(
                                    message = "Không thể tải dữ liệu. Vui lòng thử lại.",
                                    onRetry = { lazyPagingItems.retry() }
                                )
                            }
                        }
                        // --- Trạng thái tải thêm (cuộn xuống cuối danh sách) ---
                        append is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        append is LoadState.Error -> {
                            val e = lazyPagingItems.loadState.append as LoadState.Error
                            item {
                                ErrorState(
                                    message = "Có lỗi xảy ra. Nhấn để thử lại.",
                                    onRetry = { lazyPagingItems.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable để hiển thị trạng thái lỗi và nút thử lại.
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 8.dp)) {
            Text("Thử lại")
        }
    }
}