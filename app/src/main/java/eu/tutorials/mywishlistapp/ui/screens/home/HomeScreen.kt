package eu.tutorials.mywishlistapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.mywishlistapp.QuizApp
import eu.tutorials.mywishlistapp.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToQuizList: () -> Unit,
    onNavigateToAddQuiz: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: HomeViewModel = viewModel(
        factory = ViewModelFactory { HomeViewModel(container.sessionManager) }
    )

    val username by viewModel.username.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QuizApp") },
                actions = {
                    IconButton(onClick = { viewModel.logout { onLogout() } }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Вихід")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Привіт, $username!",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Що будемо робити?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            Button(
                onClick = onNavigateToQuizList,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.List, contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp))
                Text("Пройти квіз")
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = onNavigateToAddQuiz,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp))
                Text("Створити квіз")
            }
        }
    }
}