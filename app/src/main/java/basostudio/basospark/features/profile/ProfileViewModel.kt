package basostudio.basospark.features.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.data.repository.PostRepository
import basostudio.basospark.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class giữ nguyên, rất tốt!
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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    fun refreshData() {
        loadProfileData()
    }

    fun logout() {
        sessionManager.clearSession()
        _uiState.update { ProfileUiState.Error("Bạn đã đăng xuất. Vui lòng đăng nhập lại.") }
    }

    private fun loadProfileData() {
        val currentUserId = sessionManager.fetchUserDetails()?.id
        Log.d("ProfileViewModel", "Loading profile data for user ID: $currentUserId")

        if (currentUserId == null) {
            _uiState.value = ProfileUiState.Error("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập.")
            return
        }

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profileResponse = userRepository.getMyProfile()

                if (profileResponse.isSuccessful && profileResponse.body() != null) {
                    val user = profileResponse.body()!!.data

                    // TỐI ƯU: Gọi API lấy bài viết và bài đã lưu song song
                    val userPostsDeferred = async { postRepository.getPosts(1, 50, user.id, "") }
                    val savedPostsDeferred = async { postRepository.getSavedPosts(user.id, 1, 50) }

                    // Chờ cả hai kết quả trả về
                    val userPostsResponse = userPostsDeferred.await()
                    val savedPostsResponse = savedPostsDeferred.await()

                    val myPosts = if (userPostsResponse.isSuccessful) userPostsResponse.body()?.data ?: emptyList() else emptyList()
                    val savedPosts = if (savedPostsResponse.isSuccessful) savedPostsResponse.body()?.data ?: emptyList() else emptyList()

                    Log.d("ProfileViewModel", "User posts count: ${myPosts.size}")

                    _uiState.value = ProfileUiState.Success(user, myPosts, savedPosts)

                } else {
                    val errorMessage = profileResponse.errorBody()?.string() ?: "Failed to load profile"
                    _uiState.value = ProfileUiState.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error loading profile data", e)
                _uiState.value = ProfileUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}