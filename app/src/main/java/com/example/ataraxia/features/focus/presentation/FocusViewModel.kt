package com.example.ataraxia.features.focus.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.features.focus.domain.DeleteFocusSessionUseCase
import com.example.ataraxia.features.focus.domain.GetFocusSessionsUseCase
import com.example.ataraxia.features.focus.domain.SaveFocusSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusViewModel(
    getSessionsUseCase: GetFocusSessionsUseCase,
    private val saveSessionUseCase: SaveFocusSessionUseCase,
    private val deleteSessionUseCase: DeleteFocusSessionUseCase
) : ViewModel() {

    val allSessions: StateFlow<List<FocusSessionEntity>> = getSessionsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationMinutes: StateFlow<Int> = getSessionsUseCase.getTotalDurationMinutes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logSession(durationMinutes: Int, spaceName: String, notes: String = "", isFlowMode: Boolean = false, targetMinutes: Int = 0) {
        viewModelScope.launch {
            saveSessionUseCase(durationMinutes, spaceName, notes, isFlowMode, targetMinutes)
        }
    }

    fun deleteFocusSession(id: Long) {
        viewModelScope.launch {
            deleteSessionUseCase(id)
        }
    }
}
