package basostudio.basospark.features.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.repository.PostRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SearchUiState {
    object Idle : SearchUiState() // Trạng thái ban đầu, chưa tìm kiếm
    object Loading : SearchUiState()
    data class Success(val results: List<Post>) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val postRepository: PostRepository
) : ViewModel() {
    private var searchJob: Job? = null

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun onQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel() // Hủy bỏ lần tìm kiếm trước nếu người dùng gõ nhanh
        searchJob = viewModelScope.launch {
            delay(500L) // Debounce: Chờ 500ms sau khi người dùng ngừng gõ
            if (query.isNotBlank()) {
                searchPosts(query)
            } else {
                _uiState.value = SearchUiState.Idle
            }
        }
    }

    private suspend fun searchPosts(query: String) {
        _uiState.value = SearchUiState.Loading
        try {
            val response = postRepository.getPosts(1, 50, query)
            if (response.isSuccessful && response.body() != null) {
                _uiState.value = SearchUiState.Success(response.body()!!.data)
            } else {
                _uiState.value = SearchUiState.Error("Search failed.")
            }
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(e.message ?: "Error")
        }
    }
}