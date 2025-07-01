package basostudio.basospark.features.auth.login


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.remote.dto.LoginRequest
import basostudio.basospark.data.repository.AuthRepository
import basostudio.basospark.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val response = authRepository.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.data.token
                    sessionManager.saveAuthToken(token)
                    fetchAndSaveUserDetails(token)
                    _uiState.value = LoginUiState.Success(token)
                } else {
                    _uiState.value = LoginUiState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    private suspend fun fetchAndSaveUserDetails(token: String) {
        val profileResponse = userRepository.getMyProfile() // UserRepository đã có context và interceptor
        if (profileResponse.isSuccessful && profileResponse.body() != null) {
            sessionManager.saveUserDetails(profileResponse.body()!!.data)
        }
    }
}