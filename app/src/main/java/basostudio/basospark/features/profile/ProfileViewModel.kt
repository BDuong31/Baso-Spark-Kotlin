package basostudio.basospark.features.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.data.repository.PostRepository
import basostudio.basospark.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import basostudio.basospark.data.remote.dto.DataResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.Response
import javax.inject.Inject

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val user: User,
        val userPosts: List<Post>,
        val savedPosts: List<Post>
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {


    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState
    private val userId: String? = savedStateHandle["userId"]

    init {
        loadProfileData(userId)
    }


    fun logout() {
        sessionManager.clearAuthToken()
    }
    private fun loadProfileData(userId: String?) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profileResponse: retrofit2.Response<DataResponse<User>>
                if (userId != null) {
                    profileResponse = userRepository.getMyProfile()
                } else {
                    profileResponse = userRepository.getProfile(userId.toString())
                }

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val user = profileResponse.body()!!.data

                    // Lấy bài đăng của người dùng
                    val userPostsResponse = postRepository.getPosts(1, 50) // Vẫn cần lọc
                    val myPosts = if (userPostsResponse.isSuccessful) {
                        userPostsResponse.body()?.data?.filter { it.author.id == user.id } ?: emptyList()
                    } else {
                        emptyList()
                    }

                    // Lấy bài đăng đã lưu
                    val savedPostsResponse = postRepository.getSavedPosts(user.id, 1, 50)
                    val savedPosts = if (savedPostsResponse.isSuccessful) {
                        savedPostsResponse.body()?.data ?: emptyList()
                    } else {
                        emptyList()
                    }

                    _uiState.value = ProfileUiState.Success(user, myPosts, savedPosts)

                } else {
                    _uiState.value = ProfileUiState.Error("Failed to load profile.")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "An unexpected error")
            }
        }
    }
}