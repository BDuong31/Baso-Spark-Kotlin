package basostudio.basospark.data.model

data class Notification(
    val id: String,
    val receiverId: String,
    val actorId: String,
    val content: String,
    val action: String,
    val isRead: Boolean,
    val createdAt: String,
    val sender: User?
)