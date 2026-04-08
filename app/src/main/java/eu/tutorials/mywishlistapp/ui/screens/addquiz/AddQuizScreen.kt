package eu.tutorials.mywishlistapp.ui.screens.addquiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import eu.tutorials.mywishlistapp.QuizApp
import eu.tutorials.mywishlistapp.data.remote.OpenAiQuizService
import eu.tutorials.mywishlistapp.util.UiState
import eu.tutorials.mywishlistapp.util.ViewModelFactory
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuizScreen(
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val container = (context.applicationContext as QuizApp).container
    val viewModel: AddQuizViewModel = viewModel(
        factory = ViewModelFactory {
            AddQuizViewModel(
                container.quizRepository,
                container.openAiQuizService,
                container.sessionManager
            )
        }
    )

    val state by viewModel.state.collectAsState()
    val generateState by viewModel.generateState.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var questions by remember {
        mutableStateOf(
            listOf(
                QuestionDraftInput()
            )
        )
    }

    var aiTopic by remember { mutableStateOf("") }
    var questionCount by remember { mutableIntStateOf(5) }

    LaunchedEffect(state) {
        if (state is UiState.Success) {
            viewModel.resetState()
            onSaved()
        }
    }

    LaunchedEffect(generateState) {
        val current = generateState
        if (current is UiState.Success) {
            title = current.data.title
            description = current.data.description
            questions = current.data.questions
            viewModel.resetGenerateState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новий квіз") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Назва квізу") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Опис") },
                modifier = Modifier.fillMaxWidth()
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Генерація через ChatGPT (OpenAI)",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Ключ API: OPENAI_API_KEY у local.properties (не коміть у git).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = aiTopic,
                onValueChange = { aiTopic = it },
                label = { Text("Тема квізу для AI") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                minLines = 2
            )

            Text(
                text = "Кількість питань: $questionCount",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = questionCount.toFloat(),
                onValueChange = { questionCount = it.roundToInt().coerceIn(OpenAiQuizService.MIN_QUESTIONS, OpenAiQuizService.MAX_QUESTIONS) },
                valueRange = OpenAiQuizService.MIN_QUESTIONS.toFloat()..OpenAiQuizService.MAX_QUESTIONS.toFloat(),
                steps = OpenAiQuizService.MAX_QUESTIONS - OpenAiQuizService.MIN_QUESTIONS - 1,
                modifier = Modifier.fillMaxWidth()
            )

            if (generateState is UiState.Error) {
                Text(
                    text = (generateState as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            OutlinedButton(
                onClick = {
                    viewModel.generateWithOpenAi(aiTopic, questionCount)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = generateState !is UiState.Loading && state !is UiState.Loading
            ) {
                if (generateState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text("Згенерувати квіз")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Питання (вручну або після генерації)",
                style = MaterialTheme.typography.titleLarge
            )

            questions.forEachIndexed { index, question ->
                QuestionEditorCard(
                    index = index,
                    question = question,
                    canDelete = questions.size > 1,
                    onDelete = {
                        questions = questions.filterIndexed { currentIndex, _ ->
                            currentIndex != index
                        }
                    },
                    onChange = { updatedQuestion ->
                        questions = questions.mapIndexed { currentIndex, currentQuestion ->
                            if (currentIndex == index) updatedQuestion else currentQuestion
                        }
                    }
                )
            }

            OutlinedButton(
                onClick = {
                    questions = questions + QuestionDraftInput()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Додати питання")
            }

            if (state is UiState.Error) {
                Text(
                    text = (state as UiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    viewModel.saveQuiz(
                        title = title,
                        description = description,
                        questions = questions
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state !is UiState.Loading && generateState !is UiState.Loading
            ) {
                if (state is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Зберегти квіз")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuestionEditorCard(
    index: Int,
    question: QuestionDraftInput,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onChange: (QuestionDraftInput) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Питання ${index + 1}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Видалити питання"
                        )
                    }
                }
            }

            OutlinedTextField(
                value = question.text,
                onValueChange = { onChange(question.copy(text = it)) },
                label = { Text("Текст питання") },
                modifier = Modifier.fillMaxWidth()
            )

            if (question.explanation.isNotBlank()) {
                Text(
                    text = "Пояснення (з AI): ${question.explanation}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedTextField(
                value = question.optionA,
                onValueChange = { onChange(question.copy(optionA = it)) },
                label = { Text("Варіант 1") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = question.optionB,
                onValueChange = { onChange(question.copy(optionB = it)) },
                label = { Text("Варіант 2") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = question.optionC,
                onValueChange = { onChange(question.copy(optionC = it)) },
                label = { Text("Варіант 3") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = question.optionD,
                onValueChange = { onChange(question.copy(optionD = it)) },
                label = { Text("Варіант 4") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Правильна відповідь",
                style = MaterialTheme.typography.titleSmall
            )

            AnswerRadio(
                label = "Варіант 1",
                selected = question.correctIndex == 0,
                onClick = { onChange(question.copy(correctIndex = 0)) }
            )
            AnswerRadio(
                label = "Варіант 2",
                selected = question.correctIndex == 1,
                onClick = { onChange(question.copy(correctIndex = 1)) }
            )
            AnswerRadio(
                label = "Варіант 3",
                selected = question.correctIndex == 2,
                onClick = { onChange(question.copy(correctIndex = 2)) }
            )
            AnswerRadio(
                label = "Варіант 4",
                selected = question.correctIndex == 3,
                onClick = { onChange(question.copy(correctIndex = 3)) }
            )
        }
    }
}

@Composable
private fun AnswerRadio(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text = label)
    }
}