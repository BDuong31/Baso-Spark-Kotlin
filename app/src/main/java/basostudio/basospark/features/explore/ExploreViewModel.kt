package basostudio.basospark.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import basostudio.basospark.core.util.Result
import basostudio.basospark.data.model.Post
import basostudio.basospark.data.model.Topic
import basostudio.basospark.data.repository.PostRepository
import basostudio.basospark.data.repository.TopicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val topicRepository: TopicRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _topics = MutableStateFlow<List<Topic>>(emptyList())
    val topics: StateFlow<List<Topic>> = _topics.asStateFlow()

    private val _selectedTopicId = MutableStateFlow<String?>(null) // null nghĩa là "Tất cả"
    val selectedTopicId: StateFlow<String?> = _selectedTopicId.asStateFlow()

    val postsStream: Flow<PagingData<Post>> = combine(
        _searchQuery,
        _selectedTopicId
    ) { query, topicId ->
        Pair(query, topicId)
    }.flatMapLatest { (query, topicId) ->
        postRepository.getPostsStream(searchQuery = query, topicId = topicId)
    }.cachedIn(viewModelScope)

    init {
        fetchTopics()
    }

    private fun fetchTopics() {
        viewModelScope.launch {
            when (val result = topicRepository.getTopics()) {
                is Result.Success -> _topics.value = result.data.data
                else -> { /* Xử lý lỗi nếu cần */ }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onTopicSelected(topicId: String?) {
        _selectedTopicId.value = topicId
    }
}