// File: app/src/main/java/basostudio/basospark/data/repository/FileRepository.kt

package basostudio.basospark.data.repository

import android.content.Context
import android.net.Uri
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.remote.dto.DataResponse // Giả định bạn có lớp này
import basostudio.basospark.data.remote.dto.FileUploadResponse // Giả định bạn có lớp này
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Interface cho FileRepository. Đây chính là thứ mà Hilt đang không tìm thấy.
 */
interface FileRepository {
    suspend fun uploadImage(imageUri: Uri): Result<String>
}

/**
 * Lớp Implementation cho FileRepository.
 */
@Singleton
class FileRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    @ApplicationContext private val context: Context
) : FileRepository {

    override suspend fun uploadImage(imageUri: Uri): Result<String> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
            val fileBytes = inputStream?.readBytes() ?: throw Exception("Cannot read file from Uri")
            inputStream.close()

            val requestFile = fileBytes.toRequestBody(
                contentResolver.getType(imageUri)?.toMediaTypeOrNull()
            )
            val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

            val response = apiService.uploadImage(body)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data.url)
            } else {
                Result.failure(Exception("Upload failed: ${response.errorBody()?.string()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}