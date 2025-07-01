package basostudio.basospark.features.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.features.feed.components.PostItem

@Composable
fun SearchScreen(navController: NavController, viewModel: SearchViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onQueryChanged(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search for posts...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") }
        )

        when (val state = uiState) {
            is SearchUiState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Start typing to search for posts.")
                }
            }
            is SearchUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No results found.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.results) { post ->
                            PostItem(
                                post = post,
                                // TODO: Cần truyền các callback để xử lý sự kiện
                                onPostClick = { /* ... */ },
                                onLikeClick = { /* ... */ },
                                onSaveClick = { /* ... */ },
                                onAuthorClick = { /* ... */ }
                            )
                        }
                    }
                }
            }
            is SearchUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}