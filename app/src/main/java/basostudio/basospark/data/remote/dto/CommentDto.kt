package basostudio.basospark.data.remote.dto

data class CreateCommentRequest(
    val content: String,
    val parentId: String? = null
)