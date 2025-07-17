package basostudio.basospark.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import basostudio.basospark.data.model.FollowerInfo
import basostudio.basospark.data.remote.ApiService
import basostudio.basospark.data.remote.FollowPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FollowRepository @Inject constructor(private val apiService: ApiService) {

    fun getFollowersStream(userId: String): Flow<PagingData<FollowerInfo>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FollowPagingSource(apiService, userId, FollowPagingSource.FollowType.FOLLOWERS) }
        ).flow
    }

    fun getFollowingsStream(userId: String): Flow<PagingData<FollowerInfo>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { FollowPagingSource(apiService, userId, FollowPagingSource.FollowType.FOLLOWINGS) }
        ).flow
    }
}