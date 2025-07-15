package basostudio.basospark.data.repository

import android.content.Context
import android.util.Log
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.model.User
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.remote.dto.UpdateFcmTokenDto
import basostudio.basospark.data.remote.dto.UserUpdateDto
import javax.inject.Inject

class UserRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getMyProfile() = apiService.getMyProfile()
    suspend fun updateMyProfile(user: UserUpdateDto) = apiService.updateProfile(user)
    suspend fun getProfile(userId: String) = apiService.getProfile(userId)
    suspend fun hasFollowed(userId: String) = apiService.hasFollowed(userId)
    suspend fun followUser(userId: String) = apiService.followUser(userId)
    suspend fun unfollowUser(userId: String) = apiService.unfollowUser(userId)
    suspend fun updateFcmToken(token: String) {
        try {
            val requestBody = UpdateFcmTokenDto(fcmToken = token)
            val response = apiService.updateFcmToken(requestBody)

            if (response.isSuccessful) {
                Log.d("UserRepository", "FCM token đã được cập nhật thành công trên server.")
            } else {
                Log.e("UserRepository", "Cập nhật FCM token thất bại. Code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Exception khi cập nhật FCM token", e)
        }
    }
}