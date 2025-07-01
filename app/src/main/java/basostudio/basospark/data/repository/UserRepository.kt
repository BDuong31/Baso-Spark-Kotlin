package basostudio.basospark.data.repository

import android.content.Context
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.ApiService
import javax.inject.Inject

class UserRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getMyProfile() = apiService.getMyProfile()
    suspend fun getProfile(userId: String) = apiService.getProfile(userId)
    suspend fun hasFollowed(userId: String) = apiService.hasFollowed(userId)
    suspend fun followUser(userId: String) = apiService.followUser(userId)
    suspend fun unfollowUser(userId: String) = apiService.unfollowUser(userId)

}