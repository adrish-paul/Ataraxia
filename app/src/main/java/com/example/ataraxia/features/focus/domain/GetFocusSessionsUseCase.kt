package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetFocusSessionsUseCase(private val repository: FocusRepository) {
    operator fun invoke(): Flow<List<FocusSessionEntity>> = repository.getAllSessions()
    
    fun getTotalDurationMinutes(): Flow<Int> = repository.getTotalFocusDurationFlow().map { it ?: 0 }
}
