package basostudio.basospark.data.repository

import android.content.Context
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.ApiService
import javax.inject.Inject

class NotificationRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getNotifications(page: Int, limit: Int) =
        apiService.getNotifications(page, limit)
}