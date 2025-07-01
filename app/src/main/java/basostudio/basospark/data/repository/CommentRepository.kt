package basostudio.basospark.data.repository

import android.content.Context
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.remote.dto.CreateCommentRequest
import javax.inject.Inject

class CommentRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getComments(postId: String, page: Int, limit: Int) =
        apiService.getComments(postId, page, limit)

    suspend fun createComment(postId: String, request: CreateCommentRequest) =
        apiService.createComment(postId, request)
}