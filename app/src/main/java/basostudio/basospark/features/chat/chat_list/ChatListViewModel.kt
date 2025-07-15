package basostudio.basospark.features.chat.chat_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.ChatRoom
import basostudio.basospark.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatListUiState {
    object Loading : ChatListUiState
    data class Success(val chatRooms: List<ChatRoom>) : ChatListUiState
    data class Error(val message: String) : ChatListUiState
}

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
): ViewModel() {

    private val _uiState = MutableStateFlow<ChatListUiState>(ChatListUiState.Loading)
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()

    init {
        fetchChatRooms()
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            _uiState.value = ChatListUiState.Loading
            try {
                val response = chatRepository.getChatRooms()
                if (response.isSuccessful && response.body() != null) {
                    val chatRooms = response.body()?.data ?: emptyList()
                    _uiState.value = ChatListUiState.Success(chatRooms)
                } else {
                    _uiState.value = ChatListUiState.Error("Failed to load chats. Error: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = ChatListUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}