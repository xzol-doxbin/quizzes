package eu.tutorials.mywishlistapp.ui.screens.quizplay

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.local.entity.QuestionEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.local.entity.QuizResultEntity
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class QuizPlayUiState(
    val isLoading: Boolean = true,
    val quiz: QuizEntity? = null,
    val questions: List<QuestionEntity> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val score: Int = 0,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
) {
    val currentQuestion: QuestionEntity?
        get() = questions.getOrNull(currentQuestionIndex)

    val totalQuestions: Int
        get() = questions.size
}

class QuizPlayViewModel(
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizPlayUiState())
    val uiState: StateFlow<QuizPlayUiState> = _uiState

    fun loadQuiz(quizId: Int) {
        if (_uiState.value.quiz?.id == quizId && _uiState.value.questions.isNotEmpty()) return

        viewModelScope.launch {
            _uiState.value = QuizPlayUiState(isLoading = true)

            val quiz = quizRepository.getQuizById(quizId)
            val questions = quizRepository.getQuestionsForQuiz(quizId)

            if (quiz == null || questions.isEmpty()) {
                _uiState.value = QuizPlayUiState(
                    isLoading = false,
                    errorMessage = "Квіз не знайдено"
                )
                return@launch
            }

            _uiState.value = QuizPlayUiState(
                isLoading = false,
                quiz = quiz,
                questions = questions
            )
        }
    }

    fun selectOption(index: Int) {
        _uiState.value = _uiState.value.copy(selectedOptionIndex = index)
    }

    fun submitAnswer(onFinished: (score: Int, total: Int) -> Unit) {
        val state = _uiState.value
        val selectedOptionIndex = state.selectedOptionIndex ?: return
        val currentQuestion = state.currentQuestion ?: return

        val newScore = if (selectedOptionIndex == currentQuestion.correctIndex) {
            state.score + 1
        } else {
            state.score
        }

        val isLastQuestion = state.currentQuestionIndex == state.questions.lastIndex

        if (isLastQuestion) {
            viewModelScope.launch {
                val userId = sessionManager.userId.first()
                if (userId > 0 && state.quiz != null) {
                    quizRepository.saveResult(
                        QuizResultEntity(
                            userId = userId,
                            quizId = state.quiz.id,
                            score = newScore,
                            totalQuestions = state.questions.size
                        )
                    )
                }

                _uiState.value = state.copy(
                    selectedOptionIndex = selectedOptionIndex,
                    score = newScore,
                    isCompleted = true
                )
                onFinished(newScore, state.questions.size)
            }
        } else {
            _uiState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1,
                selectedOptionIndex = null,
                score = newScore
            )
        }
    }
}