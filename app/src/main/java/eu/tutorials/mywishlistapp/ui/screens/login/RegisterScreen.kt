package eu.tutorials.mywishlistapp.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: LoginViewModel = viewModel(
        factory = ViewModelFactory {
            LoginViewModel(container.userRepository, container.sessionManager)
        }
    )

    var username   by remember { mutableStateOf("") }
    var password   by remember { mutableStateOf("") }
    var confirm    by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if (state is UiState.Success) { viewModel.resetState(); onRegisterSuccess() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Реєстрація") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(value = username, onValueChange = { username = it },
                label = { Text("Логін") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = password, onValueChange = { password = it },
                label = { Text("Пароль") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = confirm, onValueChange = { confirm = it; localError = "" },
                label = { Text("Підтвердити пароль") }, modifier = Modifier.fillMaxWidth(),
                singleLine = true, isError = localError.isNotEmpty(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = if (localError.isNotEmpty()) {{ Text(localError) }} else null)

            if (state is UiState.Error) {
                Spacer(Modifier.height(8.dp))
                Text((state as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    if (password != confirm) { localError = "Паролі не збігаються"; return@Button }
                    viewModel.register(username, password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state !is UiState.Loading
            ) {
                if (state is UiState.Loading)
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                else Text("Зареєструватись")
            }
        }
    }
}