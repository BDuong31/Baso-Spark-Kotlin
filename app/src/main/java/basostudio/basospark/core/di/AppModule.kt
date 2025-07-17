package basostudio.basospark.core.di

import android.content.Context
import basostudio.basospark.core.data.SessionManager
import basostudio.basospark.core.data.SettingsManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSessionManager(@ApplicationContext context: Context): SessionManager {
        return SessionManager(context)
    }
    @Provides
    @Singleton // Đảm bảo chỉ có một instance Gson duy nhất được tạo ra
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideSettingsManager(@ApplicationContext context: Context): SettingsManager {
        return SettingsManager(context)
    }

    @Provides
    @Singleton
    fun provideGoogleSignInClient(@ApplicationContext context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Yêu cầu idToken, bạn sẽ dùng token này để gửi về server
            .requestIdToken("AIzaSyCfesLLzlFvbQxLutvFLEXUBGxdxwIU9oc") // <-- THAY THẾ BẰNG WEB CLIENT ID CỦA BẠN
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }
}