package basostudio.basospark.data.model

data class Comment(
    val id: String,
    val content: String,
    val user: User,
    val likedCount: Int,
    val createdAt: String,
    val children: List<Comment>
)