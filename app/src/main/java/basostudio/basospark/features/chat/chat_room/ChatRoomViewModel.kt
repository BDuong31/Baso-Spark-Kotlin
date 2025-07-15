package basostudio.basospark.features.chat.chat_room

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.network.SocketManager
import basostudio.basospark.data.model.ChatMessage
import basostudio.basospark.data.model.ChatRoom
import basostudio.basospark.data.model.IMessage
import basostudio.basospark.data.model.Notification
import basostudio.basospark.data.model.OnMessage
import basostudio.basospark.data.model.User
import basostudio.basospark.data.model.UserM
import basostudio.basospark.data.remote.dto.DataResponse
import basostudio.basospark.data.repository.ChatRepository
import basostudio.basospark.features.chat.chat_list.ChatListUiState
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import org.json.JSONObject
import javax.inject.Inject

sealed class MessageUiState {
    object Loading : MessageUiState()
    data class Success(val messages: List<IMessage>) : MessageUiState()
    data class Error(val message: String) : MessageUiState()
}

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val socketManager: SocketManager,
    private val gson: Gson,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
): ViewModel()  {
    private val _isLoading = MutableStateFlow(false)

    private val _uiState = MutableStateFlow<MessageUiState>(MessageUiState.Loading)
    val uiState: StateFlow<MessageUiState> = _uiState

    val myUser: User? = sessionManager.fetchUserDetails()
    private val roomId: String = checkNotNull(savedStateHandle["roomId"])
    private val _messages = MutableStateFlow<List<IMessage>>(emptyList())
    val messages: StateFlow<List<IMessage>> = _messages

    private val _chatRoom = MutableStateFlow<ChatRoom?>(null)

    init {
        joinRoom(roomId, myUser?.id ?: "")
//        myUser?.id?.let {
//            socket.emit("register", JSONObject().put("userId", it))
//        }
        socketManager.on("message") { args ->
            if (args.isNotEmpty()) {
                val rawData = args[0] // Đây có thể là một String
                if (rawData is String) {
                    try {
                        val message = gson.fromJson(rawData, IMessage::class.java)
                        Log.d("ChatRoomViewModel string", "Received message: $message")
//                        viewModelScope.launch {
//                            _messages.value = _messages.value + message
//                            if (_uiState.value is MessageUiState.Success) {
//                                _uiState.value = MessageUiState.Success(_messages.value)
//                            }
//                        }
                    } catch (e: Exception) {
                        println("Error parsing message string from socket: ${e.message}")
                    }
                } else if (rawData is JSONObject) {
                    try {
                        val response = gson.fromJson(rawData.toString(), OnMessage::class.java)
                        Log.d("ChatRoomViewModel", "Received message: $response")
                        val messages = IMessage(
                            user = UserM(
                                id = response.sender,
                                avatarUrl = response.avatar,
                                name = response.name
                            ),
                            roomId = response.roomId,
                            content = response.message,
                            imageUrl = "",
                            time = ""
                        )
                        viewModelScope.launch {
                            _messages.value = _messages.value + messages
                            if (_uiState.value is MessageUiState.Success) {
                                _uiState.value = MessageUiState.Success(_messages.value)
                            }
                        }
                    } catch (e: Exception) {
                        println("Error parsing message JSONObject from socket: ${e.message}")
                    }
                }
                else {
                    println("Received unexpected message type from socket: ${rawData::class.java.name}")
                }
            } else {
                println("Received empty arguments from socket on 'message' event.")
            }
        }
        fetchChatRooms()
        fetchMessages()
    }

    fun sendMessage(receiverId: String, messageContent: String) {
        if (myUser == null) return
        val messages = IMessage(
            user = UserM(
                id = myUser.id,
                avatarUrl = myUser.avatar ?: "",
                name = myUser.firstName + myUser.lastName
            ),
            roomId = roomId,
            content = messageContent,
            imageUrl = "",
            time = ""
        )
        val jsonObject = JSONObject().apply {
            put("roomId", roomId)
            put("name", "${myUser.firstName} ${myUser.lastName}")
            put("avatar", myUser.avatar ?: "")
            put("sender", myUser.id)
            put("receiverId", receiverId)
            put("message", messageContent)
        }
        socketManager.emit("privateMessage", jsonObject)
        viewModelScope.launch {
            _messages.value = _messages.value + messages
            if (_uiState.value is MessageUiState.Success) {
                _uiState.value = MessageUiState.Success(_messages.value)
            }
        }
    }

//    override fun onCleared() {
//        super.onCleared()
//        socketManager.disconnect()
//        socketManager.off("message")
//    }

    private fun fetchMessages() {
        viewModelScope.launch {
            _uiState.value = MessageUiState.Loading // Bắt đầu loading
            try {
                val response = chatRepository.getChatMessages(roomId)
                if (response.isSuccessful && response.body() != null) {
                    Log.d("ChatRoom: ", "${_chatRoom.value}")
                    Log.d("ChatRoomViewModel", "Response body: ${response.body()}")
                    val rawMessages = response.body()!!
                    val chatMessages = rawMessages.map { rawMessageDto ->
                        val senderIsMe = rawMessageDto.senderId == myUser?.id

                        val messageUser = if (senderIsMe) {
                            UserM(
                                id = myUser?.id ?: "",
                                avatarUrl = myUser?.avatar ?: "",
                                name = "${myUser?.lastName ?: ""} ${myUser?.firstName ?: ""}".trim()
                            )
                        } else {
                            val nameToUse = rawMessageDto.senderId?.let { "${_chatRoom.value?.messager?.lastName ?: ""} ${_chatRoom.value?.messager?.firstName ?: ""}".trim() }
                                ?: "${_chatRoom.value?.messager?.lastName ?: ""} ${_chatRoom.value?.messager?.firstName ?: ""}".trim()

                            val avatarToUse = _chatRoom.value?.messager?.avatar
                                ?: _chatRoom.value?.messager?.avatar
                                ?: "/default-avatar.png"

                            UserM(
                                id = rawMessageDto.senderId,
                                avatarUrl = avatarToUse,
                                name = nameToUse
                            )
                        }

                        IMessage(
                            user = messageUser,
                            roomId = rawMessageDto.roomId,
                            content = rawMessageDto.content,
                            imageUrl = "",
                            time = rawMessageDto.createdAt,
                        )

                    }

                    Log.d("chat message: ", "${chatMessages}")

                    _messages.value = chatMessages
                    _uiState.value = MessageUiState.Success( _messages.value)
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                    _uiState.value = MessageUiState.Error("Failed to load messages: $errorMessage")
                    println("Failed to load messages: ${response.code()} - $errorMessage")
                }
            } catch (e: Exception) {
                _uiState.value = MessageUiState.Error("Network error: ${e.message}")
                println("Network error fetching messages: ${e.message}")
            }
        }
    }

    fun fetchChatRooms() {
        viewModelScope.launch {
            try {
                val response = chatRepository.getChatRoom(roomId)
                if (response.isSuccessful && response.body() != null) {
                    val chatRooms = response.body()?.data
                    Log.d("ChatRoomViewModel", "Response body: ${response.body()}")
                    _chatRoom.value = response.body()?.data
                } else {
                }
            } catch (e: Exception) {
            }
        }
    }

    fun joinRoom(roomId: String, userId: String) {
        // Code để gửi sự kiện 'joinRoom' qua socket
        val eventData = JSONObject(mapOf("roomId" to roomId, "userId" to userId))
        socketManager.emit("joinRoom", eventData)
    }

    fun leaveRoom(roomId: String, userId: String) {
        // Code để gửi sự kiện 'leaveRoom' qua socket
        val eventData = JSONObject(mapOf("roomId" to roomId, "userId" to userId))
        socketManager.emit("leaveRoom", eventData)
    }
}