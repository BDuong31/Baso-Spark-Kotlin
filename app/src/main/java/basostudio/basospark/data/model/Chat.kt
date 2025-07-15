package basostudio.basospark.data.model

data class ChatRoom(
    val id: String,
    val creatorId: String,
    val receiverId: String,
    val type: String,
    val status: String,
    val messager: Messager,
    val messages: ChatMessage?
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val createdAt: String
)

data class Messager(
    val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val avatar: String,
    val online: Boolean,
)

data class IMessage(
    val user: UserM,
    val roomId: String,
    val content: String,
    val imageUrl: String,
    val time: String,
)

data class UserM(
    val id: String,
    val avatarUrl: String,
    val name: String,
)

data class OnMessage(
    val name: String,
    val avatar: String,
    val roomId: String,
    val sender: String,
    val receiverId: String,
    val message: String
)