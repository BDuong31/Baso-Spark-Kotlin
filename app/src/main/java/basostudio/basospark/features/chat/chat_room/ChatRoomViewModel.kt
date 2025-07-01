package basostudio.basospark.features.chat.chat_room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.network.SocketManager
import basostudio.basospark.data.model.ChatMessage
import basostudio.basospark.data.model.User
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val gson: Gson,
    savedStateHandle: SavedStateHandle
): ViewModel() {
    private val socket = SocketManager.getSocket()

    val myUser: User? = sessionManager.fetchUserDetails()
    private val roomId: String = checkNotNull(savedStateHandle["roomId"])

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        socket.connect()

        myUser?.id?.let {
            socket.emit("register", JSONObject().put("userId", it))
        }
        socket.on("message") { args ->
            val data = args[0] as JSONObject
            val message = Gson().fromJson(data.toString(), ChatMessage::class.java)
            viewModelScope.launch {
                _messages.value = _messages.value + message
            }
        }
    }

    fun sendMessage(receiverId: String, messageContent: String) {
        if (myUser == null) return

        val jsonObject = JSONObject().apply {
            put("roomId", roomId)
            put("name", myUser.username) // Gửi thêm thông tin để hiển thị
            put("avatar", myUser.avatar ?: "") // Gửi thêm thông tin
            put("sender", myUser.id)
            put("receiverId", receiverId)
            put("message", messageContent)
        }
        socket.emit("privateMessage", jsonObject)
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
        socket.off("message")
    }
}