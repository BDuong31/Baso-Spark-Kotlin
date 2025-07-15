package basostudio.basospark.features.post_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.Comment
import basostudio.basospark.data.model.Post
import basostudio.basospark.features.feed.components.PostItem
import basostudio.basospark.ui.navigation.Screen
import coil.compose.AsyncImage
import android.content.Context
import android.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import basostudio.basospark.features.create_post.CreatePostUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    viewModel: PostDetailsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val currentUser = remember { SessionManager(context).fetchUserDetails() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is PostDetailsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PostDetailsUiState.Success -> {
                PostDetailsContent(
                    navController = navController,
                    state = state,
                    paddingValues = paddingValues,
                    onLikeClick = { viewModel.toggleLike() },
                    onSaveClick = { viewModel.toggleSave() },
                    onCommentSend = { content -> viewModel.postComment(state.post.id, content) } // Sửa lỗi ở đây
                )
            }
            is PostDetailsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun PostDetailsContent(
    navController: NavController,
    state: PostDetailsUiState.Success,
    paddingValues: PaddingValues,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentSend: (String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val currentUser = remember { SessionManager(context).fetchUserDetails() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                PostItem(
                    post = state.post,
                    onPostClick = {}, // Không cần xử lý click ở đây
                    onLikeClick = onLikeClick,
                    onSaveClick = onSaveClick,
                    onCommentClick = {  }, // Nhấp vào comment cũng mở chi tiết bài đăng
                    onAuthorClick = { authorId ->
                        // Sử dụng createRoute để điều hướng đúng
                        navController.navigate(Screen.Profile.createRoute(authorId))
                    },
                    onMoreOptionsClick = {
                        // TODO: Hiển thị một menu hoặc bottom sheet với các tùy chọn như "Report", "Hide",...
                    }
                )
                Divider()
                Text("Comments", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
            }
            items(state.comments) { comment ->
                CommentItem(comment = comment)
            }
        }

        // Ô nhập bình luận
    }
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
            AsyncImage(
                model = currentUser?.avatar,
                contentDescription = "My Avatar",
                modifier = Modifier.size(40.dp).clip(CircleShape)
            )
            BasicTextField(
                value = commentText,
                onValueChange = { commentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    if (commentText.isEmpty()) {
                        Text("Bạn đang nghĩ gì...", color = androidx.compose.ui.graphics.Color.Gray, fontSize = 18.sp)
                    }
                    innerTextField()
                }
            )

            Button(
                modifier = Modifier.padding(start = 0.dp, end = 8.dp),
                onClick = {
                    onCommentSend(commentText)
                    commentText = ""
                },
                enabled = commentText.isNotBlank()
            ) {
                Text("Đăng")
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = comment.user.firstName + comment.user.lastName, fontWeight = FontWeight.Bold)
            Text(text = comment.content)
        }
    }
}
