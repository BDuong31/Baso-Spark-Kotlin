package basostudio.basospark.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import basostudio.basospark.data.model.Post
import java.io.IOException
import retrofit2.HttpException

private const val STARTING_PAGE_INDEX = 1

class PostPagingSource(
    private val apiService: ApiService,
    private val searchQuery: String? = null,
    private val topicId: String? = null
) : PagingSource<Int, Post>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val page = params.key ?: STARTING_PAGE_INDEX
        return try {
            val response = apiService.getPosts(
                page = page,
                limit = params.loadSize,
                searchQuery = if (searchQuery?.isNotBlank() == true) searchQuery else null,
                topicId = topicId
            )
            val posts = response.body()?.data ?: emptyList()

            LoadResult.Page(
                data = posts,
                prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                nextKey = if (posts.isEmpty()) null else page + 1
            )
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? {
        // Cố gắng tìm trang gần nhất với vị trí neo (anchorPosition)
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}