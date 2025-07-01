package basostudio.basospark.features.profile.other_profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import basostudio.basospark.features.profile.PostGrid
import basostudio.basospark.features.profile.ProfileHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(navController: NavController, viewModel: OtherProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = (uiState as? OtherProfileUiState.Success)?.user?.username ?: "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is OtherProfileUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is OtherProfileUiState.Success -> {
                    Column {
                        ProfileHeader(user = state.user)
                        Button(
                            onClick = { viewModel.toggleFollow() },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                        ) {
                            Text(if (state.isFollowing) "Unfollow" else "Follow")
                        }
                        PostGrid(posts = state.posts)
                    }
                }
                is OtherProfileUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.message) }
            }
        }
    }
}