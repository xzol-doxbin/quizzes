package eu.tutorials.mywishlistapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import eu.tutorials.mywishlistapp.data.local.SessionManager
import eu.tutorials.mywishlistapp.data.repository.UserRepository
import eu.tutorials.mywishlistapp.util.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val state: StateFlow<UiState<Unit>> = _state

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = UiState.Error("Заповніть всі поля"); return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            userRepository.login(username.trim(), password)
                .onSuccess { user ->
                    sessionManager.saveSession(user.id, user.username)
                    _state.value = UiState.Success(Unit)
                }
                .onFailure { _state.value = UiState.Error(it.message ?: "Помилка") }
        }
    }

    fun register(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _state.value = UiState.Error("Заповніть всі поля"); return
        }
        viewModelScope.launch {
            _state.value = UiState.Loading
            userRepository.register(username.trim(), password)
                .onSuccess { user ->
                    sessionManager.saveSession(user.id, user.username)
                    _state.value = UiState.Success(Unit)
                }
                .onFailure { _state.value = UiState.Error(it.message ?: "Помилка") }
        }
    }

    fun resetState() { _state.value = UiState.Idle }
}