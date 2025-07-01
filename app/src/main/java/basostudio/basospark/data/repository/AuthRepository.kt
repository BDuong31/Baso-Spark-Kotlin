package basostudio.basospark.data.repository
import android.content.Context
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.remote.dto.LoginRequest
import basostudio.basospark.data.remote.dto.RegisterRequest
import javax.inject.Inject

class AuthRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun login(loginRequest: LoginRequest) = apiService.login(loginRequest)
    suspend fun register(registerRequest: RegisterRequest) = apiService.register(registerRequest)
}