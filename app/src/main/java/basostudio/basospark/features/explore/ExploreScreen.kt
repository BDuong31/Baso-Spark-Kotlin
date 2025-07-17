package basostudio.basospark.features.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import basostudio.basospark.features.explore.components.ExplorePostItem
import basostudio.basospark.features.explore.ExploreViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val selectedTopicId by viewModel.selectedTopicId.collectAsState()
    val posts = viewModel.postsStream.collectAsLazyPagingItems()

    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = {
//                    TextField(
//                        value = searchQuery,
//                        onValueChange = viewModel::onSearchQueryChanged,
//                        placeholder = { Text("Tìm kiếm bài viết") },
//                        leadingIcon = { Icon(Icons.Default.Search, null) },
//                        colors = TextFieldDefaults.colors(
//                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
//                            focusedContainerColor = MaterialTheme.colorScheme.surface,
//                            unfocusedIndicatorColor = Color.Transparent,
//                            focusedIndicatorColor = Color.Transparent
//                        ),
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
//                    }
//                }
//            )
//        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Topic Filter Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedTopicId == null,
                        onClick = { viewModel.onTopicSelected(null) },
                        label = { Text("Tất cả") }
                    )
                }
                items(topics) { topic ->
                    FilterChip(
                        selected = selectedTopicId == topic.id,
                        onClick = { viewModel.onTopicSelected(topic.id) },
                        label = { Text(topic.name) }
                    )
                }
            }

            // Posts List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(count = posts.itemCount, key = { index -> posts.peek(index)?.id ?: "" }) { index ->
                    val post = posts[index]
                    if (post != null) {
                        ExplorePostItem(post = post)
                    }
                }

                // Paging LoadState Handling
                posts.apply {
                    when {
                        loadState.refresh is LoadState.Loading -> {
                            item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
                        }
                        loadState.append is LoadState.Loading -> {
                            item { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) { CircularProgressIndicator() } }
                        }
                        loadState.refresh is LoadState.Error -> {
                            val e = loadState.refresh as LoadState.Error
                            item { Text("Error: ${e.error.localizedMessage}") }
                        }
                    }
                }
            }
        }
    }
}