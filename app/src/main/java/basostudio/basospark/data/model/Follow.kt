package basostudio.basospark.data.model

data class FollowerInfo(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String?,
    val followedAt: String,
    val hasFollowedBack: Boolean
)
