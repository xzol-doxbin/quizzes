package eu.tutorials.mywishlistapp.ui.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
    val percent = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Результат: $score / $total",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Успішність: $percent%",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Результат збережено локально; якщо налаштовано Supabase, він також відправлений на сервер.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Спробувати знову")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("На головну")
        }
    }
}