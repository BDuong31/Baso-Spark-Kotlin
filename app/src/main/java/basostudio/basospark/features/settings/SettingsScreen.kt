// File: app/src/main/java/basostudio/basospark/features/settings/SettingsScreen.kt
package basostudio.basospark.features.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import basostudio.basospark.ui.navigation.Screen
import basostudio.basospark.ui.theme.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val currentTheme by viewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemePickerDialog(
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                viewModel.onThemeChange(theme)
                showThemeDialog = false
            }
        )
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item {
                SettingsCategory(title = "Account")
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Security & Password",
                    onClick = { /* TODO: Navigate to change password screen */ }
                )
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    title = "Logout",
                    onClick = {
                        viewModel.onLogout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.MainFlow.route) { inclusive = true }
                        }
                    }
                )
            }

            item {
                SettingsCategory(title = "Appearance")
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = currentTheme.name.capitalize(),
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsCategory(title = "Notifications")
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    trailingContent = {
                        var isChecked by remember { mutableStateOf(true) }
                        Switch(checked = isChecked, onCheckedChange = { isChecked = it })
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(top = 16.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val itemModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Row(
        modifier = itemModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(16.dp))
            trailingContent()
        }
    }
}

@Composable
private fun ThemePickerDialog(
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                ThemeMode.values().forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(theme.name.capitalize())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Tiện ích mở rộng để viết hoa chữ cái đầu
private fun String.capitalize(): String {
    return this.lowercase().replaceFirstChar { it.titlecase() }
}