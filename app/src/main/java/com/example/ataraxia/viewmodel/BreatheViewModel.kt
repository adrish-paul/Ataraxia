package com.example.ataraxia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import com.example.ataraxia.data.repository.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BreatheViewModel(private val repository: SessionRepository) : ViewModel() {

    val allSessions: StateFlow<List<BreatheSessionEntity>> = repository.allBreatheSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDurationSeconds: StateFlow<Int> = repository.totalBreatheDurationFlow
        .map { it ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun logSession(durationSeconds: Int, method: String, mood: String = "") {
        viewModelScope.launch {
            val session = BreatheSessionEntity(
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds,
                method = method,
                mood = mood
            )
            repository.insertBreatheSession(session)
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            repository.deleteBreatheSession(id)
        }
    }
}
