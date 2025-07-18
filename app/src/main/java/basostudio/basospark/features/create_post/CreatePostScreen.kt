package basostudio.basospark.features.create_post

import android.Manifest
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import basostudio.basospark.core.data.SessionManager // Tạm thời giữ lại, nhưng nên chuyển vào ViewModel
import basostudio.basospark.core.util.SnackbarManager
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import coil.request.CachePolicy
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(navController: NavController, viewModel: CreatePostViewModel = hiltViewModel()) {

    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    // --- QUẢN LÝ STATE ---
    var content by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageKey by remember { mutableStateOf(UUID.randomUUID().toString()) } // Key để buộc AsyncImage tải lại ảnh
    var showImageSourceSheet by remember { mutableStateOf(false) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lấy state từ ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val topics by viewModel.topics.collectAsState()
    val selectedTopic = viewModel.selectedTopic.value

    // Lấy thông tin người dùng (Lưu ý: Tốt nhất nên lấy thông tin này từ ViewModel)
    val currentUser = remember { SessionManager(context).fetchUserDetails() }

    // --- LAUNCHERS CHO VIỆC CHỌN ẢNH ---

    // Tạo file tạm cho camera
    fun createTempUri(context: Context): Uri {
        val file = File.createTempFile("JPEG_${System.currentTimeMillis()}_", ".jpg", context.cacheDir)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    // Launcher để chọn ảnh từ thư viện
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            // Chuyển Uri thành Bitmap
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                imageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // imageUri là biến trạng thái chứa Uri của file tạm
        if (success && imageUri != null) {
            // Chuyển Uri từ file tạm thành Bitmap
            context.contentResolver.openInputStream(imageUri!!)?.use { inputStream ->
                imageBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }
    }

    // Launcher để xin quyền truy cập camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val tempUri = createTempUri(context)
            imageUri = tempUri // Gán Uri trước khi mở camera
            cameraLauncher.launch(tempUri)
        } else {
            // Nên dùng Snackbar thay vì Manager tĩnh
            scope.launch { snackbarHostState.showSnackbar("Cần quyền truy cập Camera.") }
        }
    }

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is CreatePostUiState.Success -> {
                navController.popBackStack() // Quay lại màn hình trước khi thành công
            }
            is CreatePostUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
            }
            else -> {}
        }
    }

    if (showImageSourceSheet) {
        ModalBottomSheet(onDismissRequest = { showImageSourceSheet = false }) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text("Thêm ảnh", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
                ListItem(
                    headlineContent = { Text("Chụp ảnh") },
                    leadingContent = { Icon(Icons.Default.CameraAlt, null) },
                    modifier = Modifier.clickable {
                        showImageSourceSheet = false
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
                ListItem(
                    headlineContent = { Text("Chọn từ thư viện") },
                    leadingContent = { Icon(Icons.Default.PhotoLibrary, null) },
                    modifier = Modifier.clickable {
                        showImageSourceSheet = false
                        galleryLauncher.launch("image/*")
                    }
                )
            }
        }
    }

    // --- GIAO DIỆN CHÍNH ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Tạo bài viết") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            selectedTopic?.id?.let { topicId ->
                                viewModel.createPost(content, topicId, imageUri)
                            }
                        },
                        enabled = uiState !is CreatePostUiState.Loading && content.isNotBlank() && selectedTopic != null
                    ) {
                        if (uiState is CreatePostUiState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Đăng")
                        }
                    }
                }
            )
        },
        bottomBar = {

        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                currentUser?.avatar?.replace("localhost","192.168.1.111")
                AsyncImage(
                    model = currentUser?.avatar,
                    contentDescription = "My Avatar",
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // TextField để nhập nội dung
                BasicTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        if (content.isEmpty()) {
                            Text("Bạn đang nghĩ gì...", color = Color.Gray, fontSize = 18.sp)
                        }
                        innerTextField()
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            imageBitmap?.let { bmp ->
                Box(contentAlignment = Alignment.TopEnd) {
                    Image(
                        bitmap = bmp.asImageBitmap(), // <-- Hiển thị Bitmap
                        contentDescription = "Ảnh đã chọn",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(onClick = { imageBitmap = null }) { // Sửa lại để xóa bitmap
                        Icon(Icons.Default.Cancel, "Xóa ảnh", tint = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NavigationBar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(start = 24.dp, end = 24.dp, bottom = 0.dp)
                    .wrapContentWidth()
                    .clip(RoundedCornerShape(24.dp)),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                IconButton(onClick = { showImageSourceSheet = true }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Thêm ảnh")
                }

                Spacer(Modifier.width(8.dp))

                // Menu Dropdown chọn chủ đề
                ExposedDropdownMenuBox(
                    modifier = Modifier.weight(1f),
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    TextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedTopic?.name ?: "Chọn chủ đề",
                        onValueChange = {},
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        topics.forEach { topic ->
                            DropdownMenuItem(
                                text = { Text(topic.name) },
                                onClick = {
                                    viewModel.onTopicSelected(topic)
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}