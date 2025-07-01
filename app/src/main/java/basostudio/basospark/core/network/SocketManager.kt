package basostudio.basospark.core.network

import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketManager {
    private var mSocket: Socket? = null

    // Lấy thể hiện của Socket
    @Synchronized
    fun getSocket(): Socket {
        if (mSocket == null) {
            try {
                // Sử dụng namespace "/chat" như trong backend
                mSocket = IO.socket("http://192.168.1.152:3000/chat")
            } catch (e: URISyntaxException) {
                e.printStackTrace()
            }
        }
        return mSocket!!
    }

    // Kết nối đến server
    @Synchronized
    fun establishConnection() {
        mSocket?.connect()
    }

    // Ngắt kết nối
    @Synchronized
    fun closeConnection() {
        mSocket?.disconnect()
    }
}