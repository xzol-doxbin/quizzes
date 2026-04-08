package eu.tutorials.mywishlistapp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.local.model.QuizResultListItem
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
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
