package basostudio.basospark.core.data

import android.content.Context
import android.content.SharedPreferences
import basostudio.basospark.data.model.User
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class SessionManager constructor(
    @ApplicationContext context: Context
) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val AUTH_TOKEN = "auth_token"
        const val USER_DETAILS = "user_details"
        const val FCM_TOKEN = "fcm_token"
    }

    private val _userDetailsFlow = MutableStateFlow<User?>(fetchUserDetails())
    val userDetailsFlow: StateFlow<User?> = _userDetailsFlow.asStateFlow()

    fun saveFcmToken(token: String?) {
        prefs.edit().putString(FCM_TOKEN, token).apply()
    }

    fun fetchFcmToken(): String? {
        return prefs.getString(FCM_TOKEN, null)
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    fun saveUserDetails(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(USER_DETAILS, userJson).apply()
        // Phát dữ liệu người dùng mới cho bất kỳ ai đang lắng nghe
        _userDetailsFlow.value = user
    }

    fun fetchUserDetails(): User? {
        val userJson = prefs.getString(USER_DETAILS, null)
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        prefs.edit()
            .remove(AUTH_TOKEN)
            .remove(USER_DETAILS)
            .apply()
        _userDetailsFlow.value = null
    }
}