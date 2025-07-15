package basostudio.basospark.features.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.data.model.Notification
import basostudio.basospark.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class NotificationFilter { ALL, LIKES, FOLLOWS, REPLIES }

sealed class NotificationUiState {
    object Loading : NotificationUiState()
    data class Success(val notifications: List<Notification>) : NotificationUiState()
    data class Error(val message: String) : NotificationUiState()
}

@HiltViewModel
class NotificationViewModel @Inject constructor(
    application: Application,
    private val repository: NotificationRepository
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Luồng chứa danh sách thông báo gốc từ API
    private val _allNotifications = MutableStateFlow<List<Notification>>(emptyList())

    // Luồng chứa trạng thái filter hiện tại do người dùng chọn
    private val _selectedFilter = MutableStateFlow(NotificationFilter.ALL)
    val selectedFilter: StateFlow<NotificationFilter> = _selectedFilter

    // Luồng dữ liệu được lọc, sẽ tự động tính toán lại khi `_allNotifications` hoặc `_selectedFilter` thay đổi
    val filteredNotifications: StateFlow<NotificationUiState> =
        combine(_allNotifications, _selectedFilter) { all, filter ->
            val filteredList = when (filter) {
                NotificationFilter.ALL -> all
                NotificationFilter.LIKES -> all.filter { it.action == "liked" }
                NotificationFilter.FOLLOWS -> all.filter { it.action == "followed" }
                NotificationFilter.REPLIES -> all.filter { it.action == "replied" }
            }
            NotificationUiState.Success(filteredList)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotificationUiState.Loading
        )

    init {
        fetchNotifications()
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getNotifications(1, 100) // Tải nhiều hơn để có dữ liệu lọc
                if (response.isSuccessful && response.body() != null) {
                    _allNotifications.value = response.body()!!.data
                }
            } catch (e: Exception) {
                // Có thể hiển thị lỗi qua SnackbarManager ở đây nếu cần
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onFilterSelected(filter: NotificationFilter) {
        _selectedFilter.value = filter
    }
}