package basostudio.basospark.data.model

data class Post(
    val id: String,
    val content: String,
    val image: String?,
    val author: User,
    val topic: Topic,
    val isFeatured: Boolean,
    val commentCount: Int,
    val likedCount: Int,
    val hasLiked: Boolean?,
    val hasSaved: Boolean?,
    val createdAt: String,
    val updatedAt: String
)

data class Topic(
    val id: String,
    val name: String,
    val color: String
)