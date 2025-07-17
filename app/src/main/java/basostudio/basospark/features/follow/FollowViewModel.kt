package basostudio.basospark.features.follow

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import basostudio.basospark.data.model.FollowerInfo
import basostudio.basospark.data.repository.FollowRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class FollowViewModel @Inject constructor(
    private val followRepository: FollowRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val userId: String = checkNotNull(savedStateHandle["userId"])
    val username: String = checkNotNull(savedStateHandle["username"])
    val initialTabIndex: Int = savedStateHandle.get<String>("tabIndex")?.toIntOrNull() ?: 0

    val followers: Flow<PagingData<FollowerInfo>> =
        followRepository.getFollowersStream(userId).cachedIn(viewModelScope)

    val followings: Flow<PagingData<FollowerInfo>> =
        followRepository.getFollowingsStream(userId).cachedIn(viewModelScope)
}
