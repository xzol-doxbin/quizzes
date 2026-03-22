package eu.tutorials.mywishlistapp.ui.screens.login

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
import eu.tutorials.mywishlistapp.QuizApp
import eu.tutorials.mywishlistapp.util.UiState
import eu.tutorials.mywishlistapp.util.ViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory {
            LoginViewModel(container.userRepository, container.sessionManager)
        }
    )

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is UiState.Success) { viewModel.resetState(); onLoginSuccess() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("QuizApp", style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 40.dp))

        OutlinedTextField(value = username, onValueChange = { username = it },
            label = { Text("Логін") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

        if (state is UiState.Error) {
            Spacer(Modifier.height(8.dp))
            Text((state as UiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = state !is UiState.Loading) {
            if (state is UiState.Loading)
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            else Text("Увійти")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Немає акаунту? Зареєструватись")
        }
    }
}