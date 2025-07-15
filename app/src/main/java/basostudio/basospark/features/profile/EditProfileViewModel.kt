package basostudio.basospark.features.profile

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.model.User
import basostudio.basospark.data.remote.dto.UserUpdateDto
import basostudio.basospark.data.repository.FileRepository
import basostudio.basospark.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Sealed class và Event không cần thay đổi
sealed class EditProfileUiState {
    object Loading : EditProfileUiState()
    data class Success(
        val firstName: String = "",
        val lastName: String = "",
        val username: String = "",
        val bio: String = "",
        val link: String = "",
        val avatarUrl: String? = null,
        val coverUrl: String? = null,
        val newAvatarUri: Uri? = null,
        val newCoverUri: Uri? = null,
        val isSaving: Boolean = false,
        val saveComplete: Boolean = false,
        val error: String? = null // Thêm trường để chứa lỗi tạm thời
    ) : EditProfileUiState()
    // Bỏ lớp Error riêng biệt để gộp vào Success state, giúp giữ lại dữ liệu người dùng đã nhập khi có lỗi
}

sealed interface EditProfileEvent {
    data class OnFirstNameChange(val value: String) : EditProfileEvent
    data class OnLastNameChange(val value: String) : EditProfileEvent
    data class OnUsernameChange(val value: String) : EditProfileEvent
    data class OnBioChange(val value: String) : EditProfileEvent
    data class OnLinkChange(val value: String) : EditProfileEvent
    data class OnAvatarSelect(val uri: Uri) : EditProfileEvent
    data class OnCoverSelect(val uri: Uri) : EditProfileEvent
    object OnSaveClick : EditProfileEvent
    object OnErrorShown : EditProfileEvent // Event để báo cho VM biết lỗi đã được hiển thị
}


@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val fileRepository: FileRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Loading)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadInitialProfile()
    }

    fun onEvent(event: EditProfileEvent) {
        when(event) {
            is EditProfileEvent.OnSaveClick -> {
                // Lấy state hiện tại, nếu là Success thì mới xử lý
                val currentState = _uiState.value
                if (currentState is EditProfileUiState.Success) {
                    handleSaveChanges(currentState)
                }
            }
            is EditProfileEvent.OnErrorShown -> {
                // Xóa thông báo lỗi sau khi đã hiển thị
                _uiState.update {
                    if(it is EditProfileUiState.Success) it.copy(error = null) else it
                }
            }
            // Các event khác chỉ được xử lý khi state là Success
            else -> _uiState.update {
                if (it is EditProfileUiState.Success) {
                    reduce(it, event)
                } else {
                    it
                }
            }
        }
    }

    /**
     * FIX: Tách riêng hàm xử lý các event thay đổi state đơn giản
     * để onEvent gọn gàng hơn.
     */
    private fun reduce(currentState: EditProfileUiState.Success, event: EditProfileEvent): EditProfileUiState {
        return when (event) {
            is EditProfileEvent.OnFirstNameChange -> currentState.copy(firstName = event.value)
            is EditProfileEvent.OnLastNameChange -> currentState.copy(lastName = event.value)
            is EditProfileEvent.OnUsernameChange -> currentState.copy(username = event.value)
            is EditProfileEvent.OnBioChange -> currentState.copy(bio = event.value)
            is EditProfileEvent.OnLinkChange -> currentState.copy(link = event.value)
            is EditProfileEvent.OnAvatarSelect -> currentState.copy(newAvatarUri = event.uri)
            is EditProfileEvent.OnCoverSelect -> currentState.copy(newCoverUri = event.uri)
            else -> currentState
        }
    }

    private fun loadInitialProfile() {
        viewModelScope.launch {
            _uiState.value = EditProfileUiState.Loading
            try {
                // ... (logic load không đổi)
                val user = userRepository.getMyProfile().body()!!.data
                _uiState.value = EditProfileUiState.Success(
                    firstName = user.firstName ?: "",
                    lastName = user.lastName ?: "",
                    username = user.username,
                    bio = user.bio ?: "",
                    link = user.websiteUrl ?: "",
                    avatarUrl = user.avatar,
                    coverUrl = user.cover
                )
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Success(error = e.message ?: "Lỗi không xác định")
            }
        }
    }

    /**
     * FIX: Toàn bộ logic lưu trữ được làm lại cho đơn giản và an toàn.
     */
    private fun handleSaveChanges(currentState: EditProfileUiState.Success) {
        viewModelScope.launch {
            // 1. Cập nhật UI để hiển thị trạng thái đang lưu
            _uiState.update { currentState.copy(isSaving = true, error = null) }

            try {
                // 2. Upload ảnh (nếu có)
                val newAvatarUrl = currentState.newAvatarUri?.let { fileRepository.uploadImage(it).getOrThrow() }
                val newCoverUrl = currentState.newCoverUri?.let { fileRepository.uploadImage(it).getOrThrow() }

                // 3. Chuẩn bị dữ liệu để gửi đi
                val updateRequest = UserUpdateDto(
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    bio = currentState.bio,
                    link = currentState.link,
                    avatar = newAvatarUrl ?: currentState.avatarUrl,
                    cover = newCoverUrl ?: currentState.coverUrl
                )

                // 4. Gọi API cập nhật
                val response = userRepository.updateMyProfile(updateRequest)

                if (!response.isSuccessful) {
                    throw Exception("Lưu thất bại: ${response.message()}")
                }

                // 5. Lấy dữ liệu user mới nhất từ API (nếu API trả về) hoặc tự xây dựng
                val updatedUser = response.body()!!.data
                sessionManager.saveUserDetails(updatedUser)

                // 6. Cập nhật UI báo hiệu thành công
                _uiState.update { currentState.copy(isSaving = false, saveComplete = true) }

            } catch (e: Exception) {
                Log.e("EditProfileVM", "Error saving profile", e)
                // 7. Cập nhật UI nếu có lỗi, giữ lại các thông tin người dùng đã nhập
                _uiState.update { currentState.copy(isSaving = false, error = e.message) }
            }
        }
    }
}