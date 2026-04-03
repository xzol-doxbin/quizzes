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

class AddQuizViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    fun saveQuiz(
        title: String,
        description: String,
        question: String,
        optionA: String,
        optionB: String,
        optionC: String,
        optionD: String,
        correctIndex: Int
    ) {
        if (
            title.isBlank() ||
            question.isBlank() ||
            optionA.isBlank() ||
            optionB.isBlank() ||
            optionC.isBlank() ||
            optionD.isBlank()
        ) {
            _state.value = UiState.Error("Заповни всі поля")
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
                    questions = listOf(
                        QuestionEntity(
                            quizId = 0,
                            text = question.trim(),
                            optionsJson = listOf(optionA, optionB, optionC, optionD)
                                .joinToString(
                                    prefix = "[\"",
                                    postfix = "\"]",
                                    separator = "\",\""
                                ) { it.trim().replace("\"", "\\\"") },
                            correctIndex = correctIndex,
                            explanation = ""
                        )
                    )
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
}