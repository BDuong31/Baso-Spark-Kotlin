package basostudio.basospark.data.remote.dto

data class DataResponse<T>(
    val data: T
)

data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int,
    val paging: PagingInfo
)

data class PagingInfo(
    val page: Int,
    val limit: Int,
    val total: Int
)