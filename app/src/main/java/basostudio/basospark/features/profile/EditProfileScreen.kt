package basostudio.basospark.features.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import basostudio.basospark.R // Thay thế bằng R file của bạn
import coil.compose.AsyncImage

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: EditProfileViewModel = hiltViewModel() // Sử dụng Hilt để inject ViewModel
) {
    // Lắng nghe và lấy trạng thái từ ViewModel
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        val currentState = uiState
        if (currentState is EditProfileUiState.Success && currentState.saveComplete) {
            // THÊM DÒNG LOG NÀY
            Log.d("RELOAD_DEBUG", "BƯỚC 1: EditScreen chuẩn bị gửi tín hiệu và quay về.")

            // Gửi tín hiệu "profile_updated" về cho màn hình trước đó
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("profile_updated", true)

            navController.popBackStack()
        }
    }
//    // Tự động quay về màn hình trước khi lưu thành công
//    LaunchedEffect(uiState) {
//        val currentState = uiState
//        if (currentState is EditProfileUiState.Success && currentState.saveComplete) {
//            navController.popBackStack()
//        }
//    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val state = uiState) {
            is EditProfileUiState.Loading -> {
                // Hiển thị vòng xoay khi đang tải dữ liệu ban đầu
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
//            is EditProfileUiState. -> {
//                // Hiển thị thông báo lỗi
//                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
//                    Text(text = state.message, color = Color.Red, modifier = Modifier.padding(16.dp))
//                }
//            }
            is EditProfileUiState.Success -> {
                // Hiển thị nội dung chính khi đã tải dữ liệu thành công
                EditProfileContent(
                    state = state,
                    onEvent = viewModel::onEvent, // Truyền hàm onEvent xuống cho các component con
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Composable này chỉ chịu trách nhiệm hiển thị giao diện, không chứa logic.
 */
@Composable
private fun EditProfileContent(
    state: EditProfileUiState.Success,
    onEvent: (EditProfileEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    // Launcher để chọn ảnh bìa
    val coverImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onEvent(EditProfileEvent.OnCoverSelect(it)) }
    }
    // Launcher để chọn avatar
    val avatarImagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { onEvent(EditProfileEvent.OnAvatarSelect(it)) }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        // --- Phần Header ---
        EditableHeader(
            coverUrl = state.newCoverUri?.toString() ?: state.coverUrl,
            avatarUrl = state.newAvatarUri?.toString() ?: state.avatarUrl,
            isSaving = state.isSaving,
            onNavigateBack = onNavigateBack,
            onSave = { onEvent(EditProfileEvent.OnSaveClick) },
            onEditAvatar = { avatarImagePicker.launch("image/*") },
            onEditCover = { coverImagePicker.launch("image/*") }
        )

        // --- Phần Form ---
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "EDIT PROFILE",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
            )

            InfoRowTextField(
                label = "First Name",
                value = state.firstName,
                onValueChange = { onEvent(EditProfileEvent.OnFirstNameChange(it)) },
                leadingIcon = Icons.Default.PersonOutline
            )
            InfoRowTextField(
                label = "Last Name",
                value = state.lastName,
                onValueChange = { onEvent(EditProfileEvent.OnLastNameChange(it)) },
                leadingIcon = Icons.Default.PersonOutline
            )
            InfoRowTextField(
                label = "Username",
                value = state.username,
                onValueChange = { onEvent(EditProfileEvent.OnUsernameChange(it)) },
                leadingIcon = Icons.Default.AlternateEmail
            )
            InfoRowTextField(
                label = "Bio",
                value = state.bio,
                onValueChange = { onEvent(EditProfileEvent.OnBioChange(it)) },
                leadingIcon = Icons.Default.Edit
            )
            InfoRowTextField(
                label = "Link",
                value = state.link,
                onValueChange = { onEvent(EditProfileEvent.OnLinkChange(it)) },
                leadingIcon = Icons.Default.Link
            )
        }
    }
}

@Composable
private fun EditableHeader(
    coverUrl: String?,
    avatarUrl: String?,
    isSaving: Boolean,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onEditCover: () -> Unit,
    onEditAvatar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = coverUrl?.ifEmpty { null } ?: R.drawable.defaulcover,
            contentDescription = "Cover photo",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f)))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ActionIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onNavigateBack)
            Row(verticalAlignment = Alignment.CenterVertically) {
                ActionIconButton(icon = Icons.Default.PhotoCamera, onClick = onEditCover)
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    enabled = !isSaving,
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                    } else {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 16.dp)
        ) {
            AsyncImage(
                model = avatarUrl?.ifEmpty { null } ?: R.drawable.defaultavatar,
                contentDescription = "Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape)
            )
            ActionIconButton(
                icon = Icons.Default.PhotoCamera,
                onClick = onEditAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(32.dp)
            )
        }
    }
}

@Composable
private fun ActionIconButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White)
    }
}

@Composable
private fun InfoRowTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = leadingIcon, contentDescription = label, tint = Color.Gray, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = Color.Gray, fontSize = 12.sp)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            Divider(color = Color.Gray, thickness = 1.dp, modifier = Modifier.padding(top = 8.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}