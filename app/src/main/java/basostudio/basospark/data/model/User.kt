package basostudio.basospark.data.model

data class User(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val avatar: String?,
    val cover: String?,
    val bio: String?,
    val followerCount: Int = 0,
    val postCount: Int = 0
)
