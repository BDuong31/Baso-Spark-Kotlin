package basostudio.basospark.features.create_post

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.remote.dto.CreatePostRequest
import basostudio.basospark.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

sealed interface CreatePostUiState {
    object Idle : CreatePostUiState
    object Loading : CreatePostUiState
    object Success : CreatePostUiState
    data class Error(val message: String) : CreatePostUiState
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState

    fun createPost(content: String, topicId: String, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading
            try {
                // Bước 1: Nếu có ảnh, tải ảnh lên trước
                val imageUrl = if (imageUri != null) {
                    uploadImageAndGetUrl(imageUri)
                } else {
                    null
                }

                // Nếu bước 1 thất bại, imageUrl sẽ là null, dừng lại và báo lỗi
                if (imageUri != null && imageUrl == null) {
                    _uiState.value = CreatePostUiState.Error("Failed to upload image.")
                    return@launch
                }

                // Bước 2: Tạo bài đăng với URL ảnh (nếu có)
                val request = CreatePostRequest(content, imageUrl, topicId)
                val response = postRepository.createPost(request)
                Log.d("CreatePostViewModel", "Response: $response")

                if (response.isSuccessful) {
                    _uiState.value = CreatePostUiState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CreatePostViewModel", "Failed to create post: ${response.code()} - $errorBody")
                    _uiState.value = CreatePostUiState.Error("Failed to create post. Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("CreatePostViewModel", "Exception in createPost", e)
                _uiState.value = CreatePostUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private suspend fun uploadImageAndGetUrl(imageUri: Uri): String? {
        return try {
            // (2) Sử dụng 'application' đã được inject để lấy contentResolver
            val inputStream = application.contentResolver.openInputStream(imageUri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            if (fileBytes == null) {
                Log.e("CreatePostViewModel", "Could not read bytes from image Uri.")
                return null
            }

            val requestBody = fileBytes.toRequestBody(
                // Lấy kiểu MIME từ Uri
                application.contentResolver.getType(imageUri)?.toMediaTypeOrNull()
            )

            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                "image.jpg", // Tên file có thể tùy chỉnh hoặc lấy từ Uri nếu cần
                requestBody
            )

            val response = postRepository.uploadImage(multipartBody)
            if (response.isSuccessful) {
                response.body()?.data?.url
            } else {
                Log.e("CreatePostViewModel", "Image upload failed: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CreatePostViewModel", "Exception in uploadImageAndGetUrl", e)
            null
        }
    }
}