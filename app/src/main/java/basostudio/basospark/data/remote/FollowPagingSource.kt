package basostudio.basospark.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import basostudio.basospark.data.model.FollowerInfo

class FollowPagingSource(
    private val apiService: ApiService,
    private val userId: String,
    private val type: FollowType
) : PagingSource<Int, FollowerInfo>() {

    enum class FollowType { FOLLOWERS, FOLLOWINGS }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FollowerInfo> {
        val page = params.key ?: 1
        return try {
            val response = when (type) {
                FollowType.FOLLOWERS -> apiService.getFollowers(userId, page, params.loadSize)
                FollowType.FOLLOWINGS -> apiService.getFollowings(userId, page, params.loadSize)
            }
            val users = response.body()?.data ?: emptyList()
            LoadResult.Page(
                data = users,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (users.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, FollowerInfo>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
