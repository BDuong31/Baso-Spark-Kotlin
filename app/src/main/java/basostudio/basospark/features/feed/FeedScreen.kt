package basostudio.basospark.features.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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

// KHÔNG CẦN @OptIn(ExperimentalMaterial3Api::class) nữa
@Composable
fun FeedScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: FeedViewModel = hiltViewModel(),
    onPostClick: (Post) -> Unit
) {
    val lazyPagingItems: LazyPagingItems<Post> = viewModel.postsStream.collectAsLazyPagingItems()
    val updatedPostsState by viewModel.updatedPostsState.collectAsState()

    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { lazyPagingItems.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = 96.dp
            )
        ) {
            items(
                count = lazyPagingItems.itemCount,
                key = { index -> lazyPagingItems.peek(index)?.id ?: "" }
            ) { index ->
                val post = lazyPagingItems[index]
                if (post != null) {
                    val updatedState = updatedPostsState[post.id]
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
                            viewModel.toggleLikeStatus(postToShow)
                        },
                        onCommentClick = { onPostClick(postToShow) },
                        onSaveClick = {
                            viewModel.toggleSaveStatus(postToShow)
                        },
                        onAuthorClick = { authorId ->
                            navController.navigate(Screen.OtherProfile.createRoute(authorId))
                        },
                        onMoreOptionsClick = {
                            // TODO: Hiển thị một menu hoặc bottom sheet với các tùy chọn như "Report", "Hide",...
                        }
                    )
                }
            }

            lazyPagingItems.loadState.apply {
                when {
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