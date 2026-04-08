package eu.tutorials.mywishlistapp.ui.screens.addquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.remote.OpenAiQuizService
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import eu.tutorials.mywishlistapp.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QuestionDraftInput(
    val text: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctIndex: Int = 0,
    val explanation: String = ""
)

class AddQuizViewModel(
    private val quizRepository: QuizRepository,
    private val openAiQuizService: OpenAiQuizService,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    private val _generateState = MutableStateFlow<UiState<OpenAiGeneratedQuiz>>(UiState.Idle)
    val generateState: StateFlow<UiState<OpenAiGeneratedQuiz>> = _generateState

    data class OpenAiGeneratedQuiz(
        val title: String,
        val description: String,
        val questions: List<QuestionDraftInput>
    )

    fun saveQuiz(
        title: String,
        description: String,
        questions: List<QuestionDraftInput>
    ) {
        if (title.isBlank()) {
            _state.value = UiState.Error("Введи назву квізу")
            return
        }

        if (questions.isEmpty()) {
            _state.value = UiState.Error("Додай хоча б одне питання")
            return
        }

        val invalidQuestionIndex = questions.indexOfFirst { question ->
            question.text.isBlank() ||
                    question.optionA.isBlank() ||
                    question.optionB.isBlank() ||
                    question.optionC.isBlank() ||
                    question.optionD.isBlank()
        }

        if (invalidQuestionIndex != -1) {
            _state.value = UiState.Error("Заповни всі поля в питанні ${invalidQuestionIndex + 1}")
            return
        }

        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                val userId = sessionManager.userId.first()
                if (userId <= 0) {
                    _state.value = UiState.Error("Увійди в акаунт, щоб зберегти власний квіз")
                    return@launch
                }
                quizRepository.saveQuizWithQuestions(
                    quiz = QuizEntity(
                        ownerUserId = userId,
                        title = title.trim(),
                        description = description.trim().ifBlank { "Користувацький квіз" },
                        category = "Власний",
                        source = "LOCAL"
                    ),
                    questions = questions.map { question ->
                        QuestionEntity(
                            quizId = 0,
                            text = question.text.trim(),
                            optionsJson = toOptionsJson(
                                listOf(
                                    question.optionA.trim(),
                                    question.optionB.trim(),
                                    question.optionC.trim(),
                                    question.optionD.trim()
                                )
                            ),
                            correctIndex = question.correctIndex,
                            explanation = question.explanation.trim()
                        )
                    }
                )
                _state.value = UiState.Success(Unit)
            } catch (exception: Exception) {
                _state.value = UiState.Error(exception.message ?: "Не вдалося зберегти квіз")
            }
        }
    }

    fun resetState() {
        _state.value = UiState.Idle
    }

    fun generateWithOpenAi(topic: String, questionCount: Int) {
        if (topic.isBlank()) {
            _generateState.value = UiState.Error("Введи тему для генерації")
            return
        }

        viewModelScope.launch {
            _generateState.value = UiState.Loading
            runCatching {
                openAiQuizService.generateQuiz(topic.trim(), questionCount)
            }.onSuccess { payload ->
                val drafts = payload.questions.map { q ->
                    QuestionDraftInput(
                        text = q.text,
                        optionA = q.options[0],
                        optionB = q.options[1],
                        optionC = q.options[2],
                        optionD = q.options[3],
                        correctIndex = q.correctIndex,
                        explanation = q.explanation
                    )
                }
                _generateState.value = UiState.Success(
                    OpenAiGeneratedQuiz(
                        title = payload.title,
                        description = payload.description.ifBlank { "Згенеровано через OpenAI" },
                        questions = drafts
                    )
                )
            }.onFailure { e ->
                _generateState.value = UiState.Error(e.message ?: "Не вдалося згенерувати квіз")
            }
        }
    }

    fun resetGenerateState() {
        _generateState.value = UiState.Idle
    }

    private fun toOptionsJson(options: List<String>): String {
        return options.joinToString(
            prefix = "[\"",
            postfix = "\"]",
            separator = "\",\""
        ) { it.replace("\"", "\\\"") }
    }
}