package eu.tutorials.mywishlistapp.ui.screens.quizplay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.mywishlistapp.QuizApp
import eu.tutorials.mywishlistapp.ui.model.parseOptions
import eu.tutorials.mywishlistapp.util.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizPlayScreen(
    quizId: Int,
    onQuizFinished: (score: Int, total: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: QuizPlayViewModel = viewModel(
        factory = ViewModelFactory {
            QuizPlayViewModel(container.quizRepository, container.sessionManager)
        }
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.quiz?.title ?: "Квіз") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.errorMessage ?: "Помилка")
                }
            }

            else -> {
                val question = uiState.currentQuestion ?: return@Scaffold
                val options = parseOptions(question.optionsJson)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Питання ${uiState.currentQuestionIndex + 1} з ${uiState.totalQuestions}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = question.text,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        options.forEachIndexed { index, option ->
                            OptionRow(
                                text = option,
                                index = index,
                                selected = uiState.selectedOptionIndex == index,
                                answerRevealed = uiState.answerRevealed,
                                correctIndex = question.correctIndex,
                                onSelect = { viewModel.selectOption(index) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (uiState.answerRevealed) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val wasCorrect =
                                uiState.selectedOptionIndex == question.correctIndex
                            Text(
                                text = if (wasCorrect) "Правильно!" else "Неправильно",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (wasCorrect) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.error
                                }
                            )
                            if (question.explanation.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    val actionLabel = when {
                        !uiState.answerRevealed -> "Перевірити"
                        uiState.currentQuestionIndex == uiState.totalQuestions - 1 -> "Завершити"
                        else -> "Наступне питання"
                    }

                    Button(
                        onClick = {
                            viewModel.submitAnswer(onFinished = onQuizFinished)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState.selectedOptionIndex != null
                    ) {
                        Text(actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionRow(
    text: String,
    index: Int,
    selected: Boolean,
    answerRevealed: Boolean,
    correctIndex: Int,
    onSelect: () -> Unit
) {
    val highlight = when {
        answerRevealed && index == correctIndex ->
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f)
        answerRevealed && selected && index != correctIndex ->
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surface
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(highlight, RoundedCornerShape(10.dp))
            .selectable(
                selected = selected,
                enabled = !answerRevealed,
                onClick = onSelect,
                role = Role.RadioButton
            )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = null)
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}