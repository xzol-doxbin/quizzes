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
    val answerRevealed: Boolean = false,
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
        val s = _uiState.value
        if (s.quiz?.id == quizId && s.questions.isNotEmpty() && !s.isCompleted) return

        viewModelScope.launch {
            _uiState.value = QuizPlayUiState(isLoading = true)

            val sessionUserId = sessionManager.userId.first()
            val effectiveUserId = sessionUserId.takeIf { it > 0 } ?: -1
            val quiz = quizRepository.getQuizByIdVisibleToUser(quizId, effectiveUserId)
            val questions =
                if (quiz != null) quizRepository.getQuestionsForQuiz(quizId) else emptyList()

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
        if (_uiState.value.answerRevealed) return
        _uiState.value = _uiState.value.copy(selectedOptionIndex = index)
    }

    fun submitAnswer(onFinished: (score: Int, total: Int) -> Unit) {
        val state = _uiState.value
        val selectedOptionIndex = state.selectedOptionIndex ?: return
        val currentQuestion = state.currentQuestion ?: return

        if (!state.answerRevealed) {
            val newScore = if (selectedOptionIndex == currentQuestion.correctIndex) {
                state.score + 1
            } else {
                state.score
            }
            _uiState.value = state.copy(
                answerRevealed = true,
                score = newScore
            )
            return
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
                            score = state.score,
                            totalQuestions = state.questions.size
                        )
                    )
                }

                _uiState.value = state.copy(isCompleted = true)
                onFinished(state.score, state.questions.size)
            }
        } else {
            _uiState.value = state.copy(
                currentQuestionIndex = state.currentQuestionIndex + 1,
                selectedOptionIndex = null,
                answerRevealed = false
            )
        }
    }
}