package basostudio.basospark.core.network

import android.content.Context
import basostudio.basospark.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    // Sử dụng 10.0.2.2 để kết nối tới localhost từ máy ảo Android
    private const val BASE_URL = "https://baso-music.io.vn/apis/"

    fun create(context: Context): ApiService {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(context)) // <-- THÊM INTERCEPTOR Ở ĐÂY
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}