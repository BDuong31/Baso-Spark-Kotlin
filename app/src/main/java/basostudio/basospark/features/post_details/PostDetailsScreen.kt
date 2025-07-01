package basostudio.basospark.features.post_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.data.model.Comment
import basostudio.basospark.data.model.Post
import basostudio.basospark.features.feed.components.PostItem
import basostudio.basospark.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailsScreen(
    navController: NavController,
    viewModel: PostDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                    onAuthorClick = { authorId ->
                        // Sử dụng createRoute để điều hướng đúng
                        navController.navigate(Screen.Profile.createRoute(authorId))
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
        OutlinedTextField(
            value = commentText,
            onValueChange = { commentText = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Add a comment...") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        onCommentSend(commentText)
                        commentText = ""
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send comment")
                }
            }
        )
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
