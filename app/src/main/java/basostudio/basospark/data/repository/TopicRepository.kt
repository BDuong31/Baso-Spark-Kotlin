package basostudio.basospark.data.repository

import basostudio.basospark.data.remote.ApiService
import javax.inject.Inject

class TopicRepository @Inject constructor(private val apiService: ApiService) : BaseRepository() {
    suspend fun getTopics() = safeApiCall { apiService.getTopics() }
}