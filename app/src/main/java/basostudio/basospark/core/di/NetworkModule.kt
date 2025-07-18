package basostudio.basospark.core.di


import android.content.Context
import basostudio.basospark.core.network.AuthInterceptor
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://192.168.1.111:3000/"

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(context))
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    // Cung cấp các Repository
    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        return AuthRepository(apiService)
    }

    @Provides
    @Singleton
    fun providePostRepository(apiService: ApiService): PostRepository {
        return PostRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideFileRepository(apiService: ApiService, @ApplicationContext context: Context): FileRepository {
        return FileRepositoryImpl(apiService, context)
    }

    @Provides
    @Singleton
    fun provideUserRepository(apiService: ApiService): UserRepository {
        return UserRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideCommentRepository(apiService: ApiService): CommentRepository {
        return CommentRepository(apiService)
    }
    @Provides
    @Singleton
    fun provideNotificationRepository(apiService: ApiService): NotificationRepository {
        return NotificationRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideChatRepository(apiService: ApiService): ChatRepository {
        return ChatRepository(apiService)
    }

    @Provides
    @Singleton
    fun provideTopicRepository(apiService: ApiService): TopicRepository {
        return TopicRepository(apiService)
    }
}