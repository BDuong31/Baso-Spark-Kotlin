package basostudio.basospark.features.auth.login

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.ui.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginScreen(navController: NavController, viewModel: LoginViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý khi trạng thái thay đổi
    LaunchedEffect(key1 = uiState) {
        when (val state = uiState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                // TODO: Lưu token vào SharedPreferences/DataStore
                navController.navigate(Screen.MainFlow.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }
            is LoginUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit // Idle or Loading
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome Back!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(username, password) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is LoginUiState.Loading
            ) {
                Text("Login")
            }

            if (uiState is LoginUiState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}