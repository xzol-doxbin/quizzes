package eu.tutorials.mywishlistapp.ui.screens.quizlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.entity.QuizEntity
import eu.tutorials.mywishlistapp.data.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuizListUiState(
    val isLoading: Boolean = true,
    val quizzes: List<QuizEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val message: String? = null
)

class QuizListViewModel(
    private val quizRepository: QuizRepository
) : ViewModel() {

    private val isSyncing = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuizListUiState> = combine(
        quizRepository.getAllQuizzes().map { it },
        isSyncing,
        message
    ) { quizzes, syncing, currentMessage ->
        QuizListUiState(
            isLoading = false,
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
            syncFromSupabase()
        }
    }

    fun syncFromSupabase() {
        viewModelScope.launch {
            isSyncing.value = true
            message.value = null

            runCatching {
                quizRepository.syncSupabaseQuizzes()
            }.onSuccess {
                message.value = "Синхронізацію завершено"
            }.onFailure {
                message.value = "Помилка синхронізації: ${it.message}"
            }

            isSyncing.value = false
        }
    }

    fun clearMessage() {
        message.value = null
    }
}