package basostudio.basospark.features.profile.other_profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.data.repository.PostRepository
import basostudio.basospark.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OtherProfileUiState {
    object Loading : OtherProfileUiState()
    data class Success(val user: User, val posts: List<Post>, val isFollowing: Boolean) : OtherProfileUiState()
    data class Error(val message: String) : OtherProfileUiState()
}

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow<OtherProfileUiState>(OtherProfileUiState.Loading)
    val uiState: StateFlow<OtherProfileUiState> = _uiState

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.value = OtherProfileUiState.Loading
            try {
                val profileRes = userRepository.getProfile(userId)
                val followRes = userRepository.hasFollowed(userId)
                // Backend cần hỗ trợ lọc bài đăng theo userId
                val postsRes = postRepository.getPosts(1, 50)

                if (profileRes.isSuccessful && followRes.isSuccessful && postsRes.isSuccessful) {
                    val user = profileRes.body()!!.data
                    val isFollowing = followRes.body()!!.data
                    val userPosts = postsRes.body()!!.data.filter { it.author.id == userId }
                    _uiState.value = OtherProfileUiState.Success(user, userPosts, isFollowing)
                } else {
                    _uiState.value = OtherProfileUiState.Error("Failed to load profile")
                }
            } catch (e: Exception) {
                _uiState.value = OtherProfileUiState.Error(e.message ?: "Error")
            }
        }
    }

    fun toggleFollow() {
        val currentState = _uiState.value
        if (currentState is OtherProfileUiState.Success) {
            viewModelScope.launch {
                val response = if (currentState.isFollowing) {
                    userRepository.unfollowUser(userId)
                } else {
                    userRepository.followUser(userId)
                }
                if (response.isSuccessful) {
                    // Cập nhật lại trạng thái giao diện
                    _uiState.value = currentState.copy(isFollowing = !currentState.isFollowing)
                }
            }
        }
    }
}