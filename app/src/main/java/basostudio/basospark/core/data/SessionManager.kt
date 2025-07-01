package basostudio.basospark.core.data

import android.content.Context
import android.content.SharedPreferences
import basostudio.basospark.data.model.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        const val AUTH_TOKEN = "auth_token"
        const val USER_DETAILS = "user_details"
    }

    /**
     * Lưu trữ token xác thực.
     */
    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(AUTH_TOKEN, token)
        editor.apply()
    }

    /**
     * Lấy token xác thực.
     * @return Token đã lưu, hoặc null nếu không tồn tại.
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(AUTH_TOKEN, null)
    }

    fun saveUserDetails(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().putString(USER_DETAILS, userJson).apply()
    }

    fun fetchUserDetails(): User? {
        val userJson = prefs.getString(USER_DETAILS, null)
        return gson.fromJson(userJson, User::class.java)
    }

    /**
     * Xóa token xác thực (dùng cho chức năng đăng xuất).
     */
    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(AUTH_TOKEN)
        editor.apply()
    }
}