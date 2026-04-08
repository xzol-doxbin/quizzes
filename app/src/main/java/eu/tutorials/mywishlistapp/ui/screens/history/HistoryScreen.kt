package eu.tutorials.mywishlistapp.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.mywishlistapp.QuizApp
import eu.tutorials.mywishlistapp.data.local.model.QuizResultListItem
import eu.tutorials.mywishlistapp.util.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: HistoryViewModel = viewModel(
        factory = ViewModelFactory {
            HistoryViewModel(container.quizRepository, container.sessionManager)
        }
    )
    val rows by viewModel.rows.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мої результати") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (rows.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Поки немає збережених результатів. Пройди квіз під залогіненим користувачем.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rows, key = { it.id }) { item ->
                    ResultRowCard(item)
                }
            }
        }
    }
}

@Composable
private fun ResultRowCard(item: QuizResultListItem) {
    val percent = if (item.totalQuestions > 0) {
        (item.score * 100) / item.totalQuestions
    } else {
        0
    }
            val dateStr = formatCompletedAt(item.completedAt)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = item.quizTitle,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${item.score} / ${item.totalQuestions} ($percent%)",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatCompletedAt(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))
