package basostudio.basospark.data.repository

import android.content.Context
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.ApiService
import javax.inject.Inject

class ChatRepository @Inject constructor(private val apiService: ApiService) {
    suspend fun getChatRooms() = apiService.getChatRooms()
}