package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FocusViewModel(private val repository: SessionRepository) : ViewModel() {

    val allSessions: StateFlow<List<FocusSessionEntity>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationMinutes: StateFlow<Int> = repository.totalFocusDurationFlow
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logSession(durationMinutes: Int, spaceName: String, notes: String = "", isFlowMode: Boolean = false, targetMinutes: Int = 0) {
        viewModelScope.launch {
            val session = FocusSessionEntity(
                timestamp = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                spaceName = spaceName,
                notes = notes,
                isFlowMode = isFlowMode,
                targetMinutes = targetMinutes
            )
            repository.insertFocusSession(session)
        }
    }

    fun deleteFocusSession(id: Long) {
        viewModelScope.launch {
            repository.deleteFocusSession(id)
        }
    }
}
