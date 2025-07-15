package basostudio.basospark.core.network

import android.content.Context
import basostudio.basospark.core.data.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    private val sessionManager = SessionManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        val token = sessionManager.fetchAuthToken()

        if (token != null) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        val response = chain.proceed(request)

//        if (response.code == 401) {
//            sessionManager.clearAuthToken()
//        }

        return response
    }
}