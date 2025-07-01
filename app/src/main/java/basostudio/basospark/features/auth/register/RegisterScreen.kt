package basostudio.basospark.features.auth.register

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import basostudio.basospark.data.remote.dto.RegisterRequest

@Composable
fun RegisterScreen(navController: NavController, viewModel: RegisterViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(key1 = uiState) {
        when (uiState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                navController.popBackStack() // Quay lại màn hình Login
            }
            is RegisterUiState.Error -> {
                Toast.makeText(context, (uiState as RegisterUiState.Error).message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Create Account", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val request = RegisterRequest(username, email, firstName, lastName, password)
                    viewModel.register(request)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is RegisterUiState.Loading
            ) {
                Text("Sign Up")
            }

            if (uiState is RegisterUiState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Already have an account? Login")
            }
        }
    }
}