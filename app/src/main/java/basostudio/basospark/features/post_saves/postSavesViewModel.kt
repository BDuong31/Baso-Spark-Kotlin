package basostudio.basospark.features.post_saves

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.data.remote.dto.PaginatedResponse
import basostudio.basospark.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostSavesUiState {
    object Loading : PostSavesUiState()
    data class Success(
        val savedPosts: List<Post>
    ) : PostSavesUiState()
    data class Error(val message: String) : PostSavesUiState()
}

@HiltViewModel
class PostSavesViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<PostSavesUiState>(PostSavesUiState.Loading)
    val uiState: StateFlow<PostSavesUiState> = _uiState.asStateFlow()
    val user = sessionManager.fetchUserDetails()
    private val userId: String? = user?.id
    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())

    init {
        fetchSavedPosts()
    }

    private fun fetchSavedPosts() {
        viewModelScope.launch {
            _uiState.value = PostSavesUiState.Loading
            try {
                val savedPostsResponse = postRepository.getSavedPosts(userId.toString(), 1, 50)
                if (savedPostsResponse.isSuccessful) {

                    val savedPosts = savedPostsResponse.body()?.data ?: emptyList()
                    Log.d("PostSavesViewModel", "Saved Posts: $savedPosts")
                    _savedPosts.value = savedPosts
                    _uiState.value = PostSavesUiState.Success(savedPosts)
                } else {
                    _uiState.value = PostSavesUiState.Error("Failed to load saved posts.")
                }
            } catch (e: Exception) {
                _uiState.value = PostSavesUiState.Error(e.message ?: "An unexpected error")
            }
        }
    }
}

