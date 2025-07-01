package basostudio.basospark.features.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.Notification
import basostudio.basospark.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<Notification>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            try {
                val response = notificationRepository.getNotifications(1, 50)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = NotificationUiState.Success(response.body()!!.data)
                } else {
                    _uiState.value = NotificationUiState.Error("Failed to load notifications.")
                }
            } catch (e: Exception) {
                _uiState.value = NotificationUiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}