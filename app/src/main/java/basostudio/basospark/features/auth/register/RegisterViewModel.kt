package basostudio.basospark.features.auth.register
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.remote.dto.RegisterRequest
import basostudio.basospark.data.repository.AuthRepository
import basostudio.basospark.data.repository.UserRepository
import basostudio.basospark.features.auth.login.LoginUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// Trạng thái của màn hình Đăng ký
sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    data class Success (val token: String? = null) : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun register(registerRequest: RegisterRequest) {
        viewModelScope.launch {
            _uiState.value = RegisterUiState.Loading
            if (registerRequest.password.length < 6) {
                _uiState.value = RegisterUiState.Error("Password must be at least 6 characters")
                return@launch
            }

            try {
                val response = authRepository.register(registerRequest)
                if (response.isSuccessful) {
                    _uiState.value = RegisterUiState.Success()
                } else {
                    // Cần parse lỗi từ body của response để chính xác hơn
                    _uiState.value = RegisterUiState.Error("Registration failed. Username or email might already exist.")
                }
            } catch (e: Exception) {
                _uiState.value = RegisterUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

//    fun loginGoogle() {
//        viewModelScope.launch {
//            _uiState.value = RegisterUiState.Loading
//            try {
//                val response = authRepository.loginGoogle()
//                if (response.isSuccessful && response.body() != null) {
//                    val token = response.body()!!.data.token
//                    sessionManager.saveAuthToken(token)
//                    fetchAndSaveUserDetails(token)
//                    _uiState.value = RegisterUiState.Success(token)
//                } else {
//                    _uiState.value = RegisterUiState.Error("Google login failed")
//                }
//            } catch (e: Exception) {
//                _uiState.value = RegisterUiState.Error(e.message ?: "An unexpected error occurred")
//            }
//        }
//    }

    private suspend fun fetchAndSaveUserDetails(token: String) {
        val profileResponse = userRepository.getMyProfile() // UserRepository đã có context và interceptor
        if (profileResponse.isSuccessful && profileResponse.body() != null) {
            sessionManager.saveUserDetails(profileResponse.body()!!.data)
        }
    }
}