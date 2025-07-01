package basostudio.basospark.features.create_post

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, viewModel: CreatePostViewModel = hiltViewModel()) {
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is CreatePostUiState.Success -> {
                Toast.makeText(context, "Post created successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is CreatePostUiState.Error -> {
                Toast.makeText(context, (uiState as CreatePostUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val hardcodedTopicId = "0c8b7468-0868-71e4-8a4d-98ce4054a86b" // Vẫn cần thay thế
                            viewModel.createPost(content, hardcodedTopicId, imageUri)
                        },
                        enabled = uiState !is CreatePostUiState.Loading && content.isNotBlank()
                    ) {
                        Text("Post")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            imageUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(model = it),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("What's on your mind?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.height(8.dp))

            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", modifier = Modifier.size(32.dp))
            }

            if (uiState is CreatePostUiState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val hardcodedTopicId = "0197bc7d-bb1b-73ee-8ab0-a95eae6ff14c"
                    viewModel.createPost(content,"0197bc7d-bb1b-73ee-8ab0-a95eae6ff14c", imageUri)
                },
                modifier = Modifier.align(Alignment.End),
                enabled = uiState !is CreatePostUiState.Loading && content.isNotBlank()
            ) {
                if (uiState is CreatePostUiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Post")
                }
            }
        }
    }
}