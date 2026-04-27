package ua.edu.quizapp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ua.edu.quizapp.data.local.SessionManager
import ua.edu.quizapp.data.local.model.QuizResultListItem
import ua.edu.quizapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val rows: StateFlow<List<QuizResultListItem>> = sessionManager.userId
        .flatMapLatest { uid ->
            if (uid > 0) quizRepository.observeResultsWithQuizTitles(uid)
            else flowOf(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
}