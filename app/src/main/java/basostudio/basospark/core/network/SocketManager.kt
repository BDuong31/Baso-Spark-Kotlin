// Vị trí file: core/network/SocketManager.kt
package basostudio.basospark.core.network

import android.util.Log
import basostudio.basospark.core.data.SessionManager
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FIX: Chuyển từ 'object' sang 'class' và dùng Hilt để quản lý như một Singleton.
 * Điều này giúp dễ dàng inject SessionManager và làm cho code dễ kiểm thử hơn.
 */
@Singleton
class SocketManager @Inject constructor(
    private val sessionManager: SessionManager // Inject SessionManager để lấy token
) {
    private var mSocket: Socket? = null

    companion object {
        // Thay bằng IP và Port của server NestJS của bạn
        private const val SERVER_URL = "https://baso-music.io.vn/apis/chat"
    }

    /**
     * Hàm chính để khởi tạo và kết nối socket.
     * Sẽ được gọi từ ViewModel sau khi người dùng đăng nhập thành công.
     */
    fun connect() {
        val token = sessionManager.fetchAuthToken()

        if (token.isNullOrBlank()) {
            Log.e("SocketManager", "Không thể kết nối: Chưa đăng nhập (token rỗng).")
            return
        }
        if (mSocket?.isActive == true) {
            Log.d("SocketManager", "Bỏ qua kết nối: Socket đã được kết nối từ trước.")
            return
        }

        try {
            // 3. FIX: Thêm token vào header 'auth' để server NestJS có thể xác thực
            val options = IO.Options().apply {
                auth = mapOf("token" to "Bearer $token")
            }

            mSocket = IO.socket(SERVER_URL, options)

            setupEventListeners()

            // 5. Bắt đầu kết nối
            mSocket?.connect()
            Log.d("SocketManager", "Đang tiến hành kết nối socket...")

        } catch (e: Exception) {
            Log.e("SocketManager", "Lỗi khởi tạo Socket.IO", e)
        }
    }

    private fun setupEventListeners() {
        mSocket?.on(Socket.EVENT_CONNECT) {
            Log.d("SocketManager", "✅ Kết nối Socket thành công! ID: ${mSocket?.id()}")
            sessionManager.fetchUserDetails()?.id?.let { userId ->
                val registerData = JSONObject().put("userId", userId)
                emit("register", registerData)
                Log.d("SocketManager", "Đã gửi sự kiện 'register' cho userId: $userId")
            }
        }
        mSocket?.on(Socket.EVENT_DISCONNECT) { reason ->
            Log.w("SocketManager", "❌ Đã ngắt kết nối Socket. Lý do: $reason")
        }
        mSocket?.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val errorMessage = args.getOrNull(0)?.toString() ?: "Lỗi không xác định"
            Log.e("SocketManager", "Lỗi kết nối Socket: $errorMessage")
        }
    }

    /**
     * Gửi một sự kiện và dữ liệu đến server.
     */
    fun emit(event: String, data: JSONObject) {
        if (mSocket?.connected() == true) {
            mSocket?.emit(event, data)
        } else {
            Log.e("SocketManager", "Không thể gửi sự kiện '$event' vì socket chưa kết nối.")
        }
    }

    /**
     * Lắng nghe một sự kiện từ server.
     */
    fun on(event: String, listener: (Array<Any>) -> Unit) {
        mSocket?.on(event, listener)
    }

    /**
     * Bỏ lắng nghe một sự kiện khỏi server.
     */
    fun off(event: String) {
        mSocket?.off(event)
    }

    /**
     * Ngắt kết nối socket, thường được gọi khi người dùng đăng xuất.
     */
    fun disconnect() {
        mSocket?.disconnect()
        mSocket?.off() // Gỡ bỏ tất cả các listener để tránh memory leak
        mSocket = null
        Log.d("SocketManager", "Đã ngắt kết nối và dọn dẹp socket.")
    }
}