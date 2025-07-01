package basostudio.basospark

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import basostudio.basospark.core.util.SnackbarManager
import basostudio.basospark.ui.navigation.AppNavHost
import basostudio.basospark.ui.theme.BasoSocialAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import basostudio.basospark.ui.theme.ThemeMode

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by mainViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            BasoSocialAppTheme(themeMode = themeMode) {
                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                // Lắng nghe thông báo từ SnackbarManager
                val snackbarMessage by SnackbarManager.messages.collectAsState()
                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(it)
                            SnackbarManager.clearMessage()
                        }
                    }
                }

                // Sử dụng Scaffold để đặt SnackbarHost
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(paddingValues = paddingValues) // Truyền paddingValues vào AppNavHost
                    }
                }
            }
        }
    }
}