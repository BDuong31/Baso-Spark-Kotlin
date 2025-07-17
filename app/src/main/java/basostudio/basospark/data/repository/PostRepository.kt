package basostudio.basospark.data.repository

import android.content.Context
import androidx.compose.ui.autofill.ContentType
import basostudio.basospark.core.network.RetrofitInstance
import basostudio.basospark.data.remote.dto.CreatePostRequest
import okhttp3.MultipartBody
import javax.inject.Inject
import basostudio.basospark.data.remote.ApiService
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.remote.PostPagingSource
import kotlinx.coroutines.flow.Flow

class PostRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun uploadImage(imagePart: MultipartBody.Part) = apiService.uploadImage(imagePart)

    suspend fun getPosts(page: Int, limit: Int, searchQuery: String? = null, userId: String? = null, topicId: String? = null) =
        apiService.getPosts(page, limit, searchQuery, userId)
    fun getPostsStream(searchQuery: String? = null, topicId: String? = null): Flow<PagingData<Post>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostPagingSource(apiService, searchQuery, topicId) }
        ).flow
    }

    suspend fun getPostById(postId: String) = apiService.getPostById(postId)

    suspend fun createPost(request: CreatePostRequest) = apiService.createPost(request)
    suspend fun likePost(postId: String) = apiService.likePost(postId)
    suspend fun unlikePost(postId: String) = apiService.unlikePost(postId)
    suspend fun savePost(postId: String) = apiService.savePost(postId)
    suspend fun unsavePost(postId: String) = apiService.unsavePost(postId)
    suspend fun getSavedPosts(userId: String, page: Int, limit: Int) =
        apiService.getSavedPosts(userId, page, limit)
}