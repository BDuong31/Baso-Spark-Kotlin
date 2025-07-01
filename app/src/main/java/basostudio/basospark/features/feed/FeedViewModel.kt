// File: app/src/main/java/basostudio/basospark/features/feed/FeedViewModel.kt
package basostudio.basospark.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {

    // Luồng dữ liệu phân trang
    val postsStream: Flow<PagingData<Post>> = postRepository
        .getPostsStream()
        .cachedIn(viewModelScope)

    // StateFlow để theo dõi các bài đăng đã được cập nhật trạng thái
    private val _updatedPostsState = MutableStateFlow<Map<String, PostUpdateState>>(emptyMap())
    val updatedPostsState: StateFlow<Map<String, PostUpdateState>> = _updatedPostsState.asStateFlow()


    fun toggleLikeStatus(post: Post) {
        viewModelScope.launch {
            val isCurrentlyLiked = post.hasLiked ?: false
            // Cập nhật UI ngay lập tức để người dùng thấy phản hồi
            updateLocalState(post.id, isLiked = !isCurrentlyLiked, likeCount = if (isCurrentlyLiked) post.likedCount - 1 else post.likedCount + 1)

            try {
                if (isCurrentlyLiked) {
                    postRepository.unlikePost(post.id)
                } else {
                    postRepository.likePost(post.id)
                }
            } catch (e: Exception) {
                // Nếu API thất bại, khôi phục lại trạng thái ban đầu
                updateLocalState(post.id, isLiked = isCurrentlyLiked, likeCount = post.likedCount)
            }
        }
    }

    fun toggleSaveStatus(post: Post) {
        viewModelScope.launch {
            val isCurrentlySaved = post.hasSaved ?: false
            // Cập nhật UI ngay lập tức
            updateLocalState(post.id, isSaved = !isCurrentlySaved)

            try {
                if (isCurrentlySaved) {
                    postRepository.unsavePost(post.id)
                } else {
                    postRepository.savePost(post.id)
                }
            } catch (e: Exception) {
                // Nếu API thất bại, khôi phục lại trạng thái ban đầu
                updateLocalState(post.id, isSaved = isCurrentlySaved)
            }
        }
    }

    private fun updateLocalState(postId: String, isLiked: Boolean? = null, likeCount: Int? = null, isSaved: Boolean? = null) {
        val currentUpdates = _updatedPostsState.value.toMutableMap()
        val currentState = currentUpdates[postId] ?: PostUpdateState()

        currentUpdates[postId] = currentState.copy(
            hasLiked = isLiked ?: currentState.hasLiked,
            likedCount = likeCount ?: currentState.likedCount,
            hasSaved = isSaved ?: currentState.hasSaved
        )
        _updatedPostsState.value = currentUpdates
    }
}

// Lớp dữ liệu để giữ trạng thái cập nhật
data class PostUpdateState(
    val hasLiked: Boolean? = null,
    val likedCount: Int? = null,
    val hasSaved: Boolean? = null
)