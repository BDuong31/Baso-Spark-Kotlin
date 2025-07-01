package basostudio.basospark.data.repository

import basostudio.basospark.core.util.Result
import retrofit2.Response
import java.io.IOException

abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.Success(body)
                } else {
                    Result.Error("Response body is empty")
                }
            } else {
                Result.Error("API Error: ${response.code()} ${response.message()}")
            }
        } catch (e: IOException) {
            Result.Error("Network Error: Please check your connection")
        } catch (e: Exception) {
            Result.Error(e.message ?: "An unknown error occurred")
        }
    }
}