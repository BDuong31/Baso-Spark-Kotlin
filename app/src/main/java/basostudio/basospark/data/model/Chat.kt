package basostudio.basospark.data.model

data class ChatRoom(
    val id: String,
    val messager: User,
    val messages: ChatMessage?
)

data class ChatMessage(
    val id: String,
    val roomId: String,
    val senderId: String,
    val content: String,
    val createdAt: String
)
