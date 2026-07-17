package com.example.ataraxia.features.breathe.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.features.breathe.domain.DeleteBreatheSessionUseCase
import com.example.ataraxia.features.breathe.domain.GetBreatheSessionsUseCase
import com.example.ataraxia.features.breathe.domain.SaveBreatheSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BreatheViewModel(
    getSessionsUseCase: GetBreatheSessionsUseCase,
    private val saveSessionUseCase: SaveBreatheSessionUseCase,
    private val deleteSessionUseCase: DeleteBreatheSessionUseCase
) : ViewModel() {

    val allSessions: StateFlow<List<BreatheSessionEntity>> = getSessionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationSeconds: StateFlow<Int> = getSessionsUseCase.getTotalDuration()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logSession(durationSeconds: Int, method: String, mood: String = "") {
        viewModelScope.launch {
            saveSessionUseCase(durationSeconds, method, mood)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            deleteSessionUseCase(id)
        }
    }
}
