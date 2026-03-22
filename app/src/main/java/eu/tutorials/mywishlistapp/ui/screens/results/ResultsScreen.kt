package eu.tutorials.mywishlistapp.ui.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultsScreen(
    score: Int,
    total: Int,
    quizId: Int,
    onHome: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Результат: $score / $total", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) { Text("Спробувати знову") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth()) { Text("На головну") }
    }
}