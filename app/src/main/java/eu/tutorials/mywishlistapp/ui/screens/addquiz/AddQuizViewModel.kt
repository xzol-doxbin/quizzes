package eu.tutorials.mywishlistapp.ui.screens.addquiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import eu.tutorials.mywishlistapp.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuestionDraftInput(
    val text: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctIndex: Int = 0
)

class AddQuizViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

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
                quizRepository.saveQuizWithQuestions(
                    quiz = QuizEntity(
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
                            explanation = ""
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

    private fun toOptionsJson(options: List<String>): String {
        return options.joinToString(
            prefix = "[\"",
            postfix = "\"]",
            separator = "\",\""
        ) { it.replace("\"", "\\\"") }
    }
}