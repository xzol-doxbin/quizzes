package eu.tutorials.mywishlistapp.ui.screens.quizlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizListUiState(
    val isLoading: Boolean = true,
    val quizzes: List<QuizEntity> = emptyList()
)

class QuizListViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    val uiState: StateFlow<QuizListUiState> = quizRepository.getAllQuizzes()
        .map { quizzes ->
            QuizListUiState(
                isLoading = false,
                quizzes = quizzes
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QuizListUiState()
        )

    init {
        viewModelScope.launch {
            quizRepository.seedDemoQuizzesIfEmpty()
        }
    }
}