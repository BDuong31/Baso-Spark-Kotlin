package basostudio.basospark.data.remote.dto
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("f2a") val f2a: Boolean
)