package eu.tutorials.mywishlistapp.ui.screens.quizlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizListUiState(
    val isInitialLoading: Boolean = true,
    val quizzes: List<QuizEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val message: String? = null
)

class QuizListViewModel(
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)
    private val initialWorkComplete = MutableStateFlow(false)

    private val quizzesForUser = sessionManager.userId.flatMapLatest { uid ->
        val effectiveId = uid.takeIf { it > 0 } ?: -1
        quizRepository.observeQuizzesForUser(effectiveId)
    }

    val uiState: StateFlow<QuizListUiState> = combine(
        quizzesForUser,
        isSyncing,
        message,
        initialWorkComplete
    ) { quizzes, syncing, currentMessage, workDone ->
        QuizListUiState(
            isInitialLoading = !workDone,
            quizzes = quizzes,
            isSyncing = syncing,
            message = currentMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuizListUiState()
    )

    init {
        viewModelScope.launch {
            quizRepository.seedDemoQuizzesIfEmpty()
            performSync()
            initialWorkComplete.value = true
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch { performSync() }
    }

    private suspend fun performSync() {
        isSyncing.value = true
        message.value = null
        try {
            runCatching {
                quizRepository.syncSupabaseQuizzes()
            }.onSuccess {
                message.value = "Синхронізацію завершено"
            }.onFailure {
                message.value = "Помилка синхронізації: ${it.message}"
            }
        } finally {
            isSyncing.value = false
        }
    }

    fun clearMessage() {
        message.value = null
    }
}