package basostudio.basospark.features.create_post

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.util.Result
import basostudio.basospark.data.model.Topic
import basostudio.basospark.data.remote.dto.CreatePostRequest
import basostudio.basospark.data.repository.PostRepository
import basostudio.basospark.data.repository.TopicRepository
import dagger.hilt.android.internal.Contexts.getApplication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val topicRepository: TopicRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics = _topics.asStateFlow()

    var selectedTopic = mutableStateOf<Topic?>(null)
        private set

    init {
        // Tải danh sách chủ đề ngay khi ViewModel được tạo
        fetchTopics()
    }

    private fun fetchTopics() {
        viewModelScope.launch {
            when (val result = topicRepository.getTopics()) {
                is Result.Success -> {
                    _topics.value = result.data.data
                    // Tự động chọn chủ đề đầu tiên làm mặc định nếu có
                    if (result.data.data.isNotEmpty()) {
                        selectedTopic.value = result.data.data.first()
                    }
                }
                is Result.Error -> {
                    _uiState.value = CreatePostUiState.Error("Không tải được chủ đề: ${result.message}")
                }
                else -> {}
            }
        }
    }

    fun onTopicSelected(topic: Topic) {
        selectedTopic.value = topic
    }
    // --- KẾT THÚC BỔ SUNG ---


    fun createPost(content: String, topicId: String, imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = CreatePostUiState.Loading
            try {
                val imageUrl = if (imageUri != null) {
                    uploadImageAndGetUrl(imageUri)
                } else {
                    null
                }

                if (imageUri != null && imageUrl == null) {
                    _uiState.value = CreatePostUiState.Error("Tải ảnh thất bại.")
                    return@launch
                }

                val request = CreatePostRequest(content, imageUrl, topicId)
                val response = postRepository.createPost(request)
                Log.d("CreatePostViewModel", "Response: $response")

                if (response.isSuccessful) {
                    _uiState.value = CreatePostUiState.Success
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CreatePostViewModel", "Tạo bài viết thất bại: ${response.code()} - $errorBody")
                    _uiState.value = CreatePostUiState.Error("Tạo bài viết thất bại: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("CreatePostViewModel", "Ngoại lệ trong createPost", e)
                _uiState.value = CreatePostUiState.Error(e.message ?: "Đã xảy ra lỗi không mong muốn")
            }
        }
    }

    private suspend fun uploadImageAndGetUrl(imageUri: Uri): String? {
        return try {
            val inputStream = application.contentResolver.openInputStream(imageUri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            if (fileBytes == null) {
                Log.e("CreatePostViewModel", "Không thể đọc được file ảnh.")
                return null
            }

            val requestBody = fileBytes.toRequestBody(
                application.contentResolver.getType(imageUri)?.toMediaTypeOrNull()
            )

            val multipartBody = MultipartBody.Part.createFormData(
                "file", "image.jpg", requestBody
            )

            val response = postRepository.uploadImage(multipartBody)
            if (response.isSuccessful) {
                response.body()?.data?.url
            } else {
                Log.e("CreatePostViewModel", "Tải ảnh thất bại: ${response.code()} - ${response.message()}")
                null
            }
        } catch (e: Exception) {
            Log.e("CreatePostViewModel", "Ngoại lệ trong uploadImageAndGetUrl", e)
            null
        }
    }
}