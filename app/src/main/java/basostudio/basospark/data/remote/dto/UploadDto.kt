package basostudio.basospark.data.remote.dto

data class FileUploadResponse(
    val filename: String,
    val url: String,
    val ext: String,
    val contentType: String,
    val size: Long
)