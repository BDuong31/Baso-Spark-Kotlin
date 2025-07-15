package basostudio.basospark.features.auth.login


import android.app.Application
import android.content.Intent
import android.util.Log
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
import basostudio.basospark.core.network.SocketManager
import basostudio.basospark.data.remote.dto.RegisterRequest
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn // <-- THÊM IMPORT NÀY
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject

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
    private val googleSignInClient: GoogleSignInClient,
    private val socketManager: SocketManager
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
                    syncFcmToken()
                    socketManager.connect()
                    _uiState.value = LoginUiState.Success(token)
                } else {
                    _uiState.value = LoginUiState.Error("Invalid username or password")
                }
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun onGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)

                if (account != null) {
                    // Tạo request body với đầy đủ thông tin
                    val request = RegisterRequest(
                        firstName = account.givenName ?: "",
                        lastName = account.familyName ?: "",
                        email = account.email ?: "",
                        username = account.email?.split("@")?.get(0) ?: "user${System.currentTimeMillis()}",
                        avatar = account.photoUrl?.toString(),
                        password = "00000000"
                    )

                    // Gửi request này về server
                    val response = authRepository.loginGoogle(request) // Giả sử hàm này nhận vào GoogleLoginRequest
                    if (response.isSuccessful && response.body() != null) {
                        val token = response.body()!!.data.token
                        sessionManager.saveAuthToken(token)
                        fetchAndSaveUserDetails(token)
                        _uiState.value = LoginUiState.Success(token)
                    } else {
                        _uiState.value = LoginUiState.Error("Invalid username or password")
                    }
                } else {
                    _uiState.value = LoginUiState.Error("Could not get Google account details.")
                }
            } catch (e: ApiException) {
                _uiState.value = LoginUiState.Error("Google Sign-In failed: ${e.statusCode}")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error(e.message ?: "An unknown error occurred.")
            }
        }
    }

    private suspend fun fetchAndSaveUserDetails(token: String) {
        val profileResponse = userRepository.getMyProfile() // UserRepository đã có context và interceptor
        if (profileResponse.isSuccessful && profileResponse.body() != null) {
            sessionManager.saveUserDetails(profileResponse.body()!!.data)
        }
    }

    private fun syncFcmToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val fcmToken = sessionManager.fetchFcmToken()
            if (!fcmToken.isNullOrBlank()) {
                Log.d("FCM_SYNC", "Phát hiện có token chờ, đang gửi lên server...")
                userRepository.updateFcmToken(fcmToken)
            }
        }
    }
}