package basostudio.basospark.core.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import basostudio.basospark.R
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.data.remote.dto.UpdateFcmTokenDto
import basostudio.basospark.data.repository.UserRepository
import basostudio.basospark.MainActivity // Giả sử đây là Activity chính của bạn
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Sử dụng Hilt để inject UserRepository và SessionManager
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var sessionManager: SessionManager

    /**
     * Được gọi khi Firebase cấp một Token mới hoặc khi token được làm mới.
     * Đây là lúc bạn phải gửi token này lên server NestJS của mình.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Token mới được tạo: $token")

        // Lưu token tạm thời và gửi lên server
        sessionManager.saveFcmToken(token)
        sendTokenToServer(token)
    }

    /**
     * Được gọi khi ứng dụng nhận được tin nhắn và đang chạy ở chế độ nền trước (foreground).
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM_MESSAGE", "Nhận được thông báo từ server!")

        // Lấy tiêu đề và nội dung từ payload `notification`
        remoteMessage.notification?.let {
            Log.d("FCM_MESSAGE", "Tiêu đề: ${it.title}, Nội dung: ${it.body}")
            // Hiển thị thông báo lên thanh trạng thái
            sendNotification(it.title, it.body)
        }
    }

    /**
     * Hàm này gửi token lên server của bạn.
     */
    private fun sendTokenToServer(token: String) {
        // Chỉ gửi token nếu người dùng đã đăng nhập (có access token)
        if (sessionManager.fetchAuthToken() != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Giả định bạn đã có hàm `updateFcmToken` trong UserRepository
                    userRepository.updateFcmToken(token)
                    Log.d("FCM_TOKEN", "Đã gửi token lên server thành công.")
                } catch (e: Exception) {
                    Log.e("FCM_TOKEN", "Gửi token lên server thất bại", e)
                }
            }
        } else {
            Log.d("FCM_TOKEN", "Người dùng chưa đăng nhập, chưa gửi token lên server.")
        }
    }

    /**
     * Hàm này tạo và hiển thị một thông báo trên thanh trạng thái.
     */
    private fun sendNotification(title: String?, messageBody: String?) {
        // Intent để mở ứng dụng khi người dùng nhấn vào thông báo
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        val channelId = "baso_spark_channel" // ID của kênh thông báo
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.baso) // Đảm bảo icon này tồn tại và hợp lệ
            .setContentTitle(title ?: "Thông báo mới từ BasoSpark")
            .setContentText(messageBody)
            .setAutoCancel(true) // Tự động xóa thông báo khi người dùng nhấn vào
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ưu tiên cao để thông báo "nhảy" lên
            .setContentIntent(pendingIntent) // Thêm PendingIntent để có thể nhấn vào

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Đối với Android O (API 26) trở lên, cần phải có NotificationChannel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Thông báo chung",
                NotificationManager.IMPORTANCE_HIGH // QUAN TRỌNG: Dùng IMPORTANCE_HIGH để thông báo nổi lên
            )
            notificationManager.createNotificationChannel(channel)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("FCM_MESSAGE", "Không có quyền POST_NOTIFICATIONS để hiển thị thông báo.")
            // LƯU Ý: Bạn không thể yêu cầu quyền từ một Service.
            // Việc yêu cầu quyền phải được thực hiện từ một Activity.
            return
        }
        notificationManager.notify(System.currentTimeMillis().toInt() /* ID duy nhất cho thông báo */, notificationBuilder.build())
        Log.d("FCM_MESSAGE", "Đã cố gắng hiển thị thông báo.")
    }
}