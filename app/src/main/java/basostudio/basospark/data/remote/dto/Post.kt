package basostudio.basospark.data.remote.dto

data class CreatePostRequest(
    val content: String,
    val image: String?,
    val topicId: String
)