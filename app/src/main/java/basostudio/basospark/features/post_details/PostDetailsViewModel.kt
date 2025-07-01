package basostudio.basospark.features.post_details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.Comment
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.remote.dto.CreateCommentRequest
import basostudio.basospark.data.repository.CommentRepository
import basostudio.basospark.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostDetailsUiState {
    object Loading : PostDetailsUiState()
    data class Success(val post: Post, val comments: List<Comment>) : PostDetailsUiState()
    data class Error(val message: String) : PostDetailsUiState()
}

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Giả sử chúng ta sẽ truyền đối tượng Post qua navigation,
    // trong một ứng dụng thực tế, bạn có thể fetch lại nó bằng postId.
    private val postId: String = checkNotNull(savedStateHandle["postId"])
    private val _uiState = MutableStateFlow<PostDetailsUiState>(PostDetailsUiState.Loading)
    val uiState: StateFlow<PostDetailsUiState> = _uiState

    init {
        // Chỉ tải nếu postId hợp lệ
        if (postId.isNotBlank()) {
            loadPostDetails()
        } else {
            _uiState.value = PostDetailsUiState.Error("Post ID is missing.")
        }
    }

    fun loadPostDetails() {
        viewModelScope.launch {
            _uiState.value = PostDetailsUiState.Loading
            try {
                val postResponse = postRepository.getPostById(postId)
                if (postResponse.isSuccessful && postResponse.body() != null) {
                    val post = postResponse.body()!!.data

                    val commentsResponse = commentRepository.getComments(postId, 1, 50)
                    val comments = if (commentsResponse.isSuccessful) {
                        commentsResponse.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }
                    _uiState.value = PostDetailsUiState.Success(post, comments)
                } else {
                    _uiState.value = PostDetailsUiState.Error("Failed to load post.")
                }
            } catch (e: Exception) {
                _uiState.value = PostDetailsUiState.Error(e.message ?: "An unexpected error")
            }
        }
    }

    fun postComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val response = commentRepository.createComment(postId, CreateCommentRequest(content))
                if (response.isSuccessful && response.body() != null) {
                    (_uiState.value as? PostDetailsUiState.Success)?.post?.let {
                        loadPostDetails()
                    }
                } else {
                    // Xử lý lỗi khi post comment
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }

    fun toggleLike() {
        val currentState = _uiState.value
        if (currentState is PostDetailsUiState.Success) {
            val post = currentState.post
            viewModelScope.launch {
                val response = if (post.hasLiked == true) postRepository.unlikePost(post.id) else postRepository.likePost(post.id)
                if (response.isSuccessful) {
                    // Cập nhật lại trạng thái của bài đăng
                    val updatedPost = post.copy(
                        hasLiked = !(post.hasLiked ?: false),
                        likedCount = if (post.hasLiked != true) post.likedCount + 1 else post.likedCount - 1
                    )
                    _uiState.value = currentState.copy(post = updatedPost)
                }
            }
        }
    }

    fun toggleSave() {
        val currentState = _uiState.value
        if (currentState is PostDetailsUiState.Success) {
            val post = currentState.post
            viewModelScope.launch {
                val response = if (post.hasSaved == true) postRepository.unsavePost(post.id) else postRepository.savePost(post.id)
                if (response.isSuccessful) {
                    val updatedPost = post.copy(hasSaved = !(post.hasSaved ?: false))
                    _uiState.value = currentState.copy(post = updatedPost)
                }
            }
        }
    }

    fun setInitialPost(post: Post) {
        if (_uiState.value is PostDetailsUiState.Loading) {
            _uiState.value = PostDetailsUiState.Success(post, emptyList())
            loadPostDetails()
        }
    }
}