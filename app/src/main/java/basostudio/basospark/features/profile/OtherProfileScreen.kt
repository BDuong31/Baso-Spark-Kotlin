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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
//import basostudio.basospark.features.profile.EditProfileScreen
import basostudio.basospark.features.profile.ProfileContent
import basostudio.basospark.features.profile.ProfileHeader
import basostudio.basospark.features.profile.ProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(navController: NavController, viewModel: OtherProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    when (val state = uiState) {
        is OtherProfileUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is OtherProfileUiState.Success -> {
            ProfileContent(
                navController = navController,
                user = state.user,
                userPosts = state.posts,
                savedPosts = emptyList()
            )
        }
        is OtherProfileUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message)
            }
        }
    }
}


