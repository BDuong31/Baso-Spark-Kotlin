package basostudio.basospark.data.remote

import basostudio.basospark.data.model.ChatMessage
import basostudio.basospark.data.model.ChatRoom
import basostudio.basospark.data.model.Comment
import basostudio.basospark.data.model.FollowerInfo
import basostudio.basospark.data.model.IMessage
import basostudio.basospark.data.model.Notification
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.User
import basostudio.basospark.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE
import basostudio.basospark.data.model.Topic

interface ApiService {
    // === Auth ===
    @POST("v1/authenticate")
    suspend fun login(@Body loginRequest: LoginRequest): Response<DataResponse<AuthResponse>>

    @POST("v1/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<DataResponse<User>>

    @POST("v1/google-login")
    suspend fun loginGoogle(): Response<DataResponse<AuthResponse>>

    // === Upload ===
    @Multipart
    @POST("v1/upload-file")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<DataResponse<FileUploadResponse>>

    // === Topic ===
    @GET("v1/topics")
    suspend fun getTopics(): Response<PaginatedResponse<Topic>>

    // === Posts ===
    @GET("v1/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("userId") userId: String? = null,
        @Query("str") searchQuery: String? = null, // Thêm query param
        @Query("topicId") topicId: String? = null
    ): Response<PaginatedResponse<Post>>

    @POST("v1/posts")
    suspend fun createPost(@Body createPostRequest: CreatePostRequest): Response<DataResponse<String>>

    @GET("v1/posts/{postId}")
    suspend fun getPostById(@Path("postId") postId: String): Response<DataResponse<Post>>

    @POST("v1/posts/{postId}/like")
    suspend fun likePost(@Path("postId") postId: String): Response<DataResponse<Boolean>>

    @DELETE("v1/posts/{postId}/unlike")
    suspend fun unlikePost(@Path("postId") postId: String): Response<DataResponse<Boolean>>

    @POST("v1/posts/{postId}/save")
    suspend fun savePost(@Path("postId") postId: String): Response<DataResponse<Boolean>>

    @DELETE("v1/posts/{postId}/save")
    suspend fun unsavePost(@Path("postId") postId: String): Response<DataResponse<Boolean>>

    @GET("v1/users/{userId}/saved-posts")
    suspend fun getSavedPosts(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Post>>

    // === Comments ===
    @GET("v1/comments/{postId}/replies")
    suspend fun getComments(
        @Path("postId") postId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Comment>>

    @POST("v1/posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: String,
        @Body createCommentRequest: CreateCommentRequest
    ): Response<DataResponse<Comment>>

    // === User ===
    @GET("v1/profile")
    suspend fun getMyProfile(): Response<DataResponse<User>>

    @GET("v1/rpc/users/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<DataResponse<User>>

    @GET("v1/users/{userId}/has-followed")
    suspend fun hasFollowed(@Path("userId") userId: String): Response<DataResponse<Boolean>>

    @POST("v1/users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): Response<DataResponse<Boolean>>

    @DELETE("v1/users/{userId}/unfollow")
    suspend fun unfollowUser(@Path("userId") userId: String): Response<DataResponse<Boolean>>

    @PATCH("v1/profile")
    suspend fun updateProfile(@Body user: UserUpdateDto): Response<DataResponse<User>>

    @POST("v1/fcm-token")
    suspend fun updateFcmToken(@Body body: UpdateFcmTokenDto): Response<Unit>
    // === Chat ===
    @GET("v1/chat-rooms")
    suspend fun getChatRooms(): Response<DataResponse<List<ChatRoom>>>
    
    @GET("v1/rpc/chat-rooms/{roomId}")
    suspend fun getChatRoom(@Path("roomId") roomId: String): Response<DataResponse<ChatRoom>>

    @GET("v1/chat-messages/{roomId}")
    suspend fun getChatMessages(@Path("roomId") roomId: String): Response<List<ChatMessage>>
    // === Notifications ===
    @GET("v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<PaginatedResponse<Notification>>

    // === Following ===
    @GET("v1/users/{userId}/followers")
    suspend fun getFollowers(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<FollowerInfo>>

    @GET("v1/users/{userId}/followings")
    suspend fun getFollowings(
        @Path("userId") userId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<FollowerInfo>>
}