package com.example.rustypl8s.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rustypl8s.MainApplication
import com.example.rustypl8s.WorkoutSession
import com.example.rustypl8s.data.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface WorkoutUiState {
    object Idle : WorkoutUiState
    object Loading : WorkoutUiState
    data class Success(val session: WorkoutSession) : WorkoutUiState
    data class Error(val message: String) : WorkoutUiState
}

class WorkoutViewModel(
    private val repository: WorkoutRepository = WorkoutRepository(MainApplication.engine)
) : ViewModel() {

    private val _uiState = MutableStateFlow<WorkoutUiState>(WorkoutUiState.Idle)
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    fun startWorkout(name: String) {
        viewModelScope.launch {
            _uiState.value = WorkoutUiState.Loading
            repository.startWorkout(name)
                .onSuccess { session ->
                    _uiState.value = WorkoutUiState.Success(session)
                }
                .onFailure { error ->
                    _uiState.value = WorkoutUiState.Error(error.message ?: "Failed to start workout")
                }
        }
    }

    fun addExercise(exerciseId: String) {
        val currentState = _uiState.value
        if (currentState is WorkoutUiState.Success) {
            viewModelScope.launch {
                repository.addExercise(currentState.session.id, exerciseId)
                    .onSuccess { 
                        // Refresh session to get updated exercise blocks
                        refreshSession(currentState.session.id)
                    }
                    .onFailure { error ->
                        _uiState.value = WorkoutUiState.Error(error.message ?: "Failed to add exercise")
                    }
            }
        }
    }

    private suspend fun refreshSession(sessionId: String) {
        repository.getSession(sessionId)
            .onSuccess { session ->
                _uiState.value = WorkoutUiState.Success(session)
            }
            .onFailure { error ->
                _uiState.value = WorkoutUiState.Error(error.message ?: "Failed to refresh session")
            }
    }
}
