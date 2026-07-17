package com.example.ataraxia.features.breathe.domain

import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetBreatheSessionsUseCase(private val repository: BreatheRepository) {
    operator fun invoke(): Flow<List<BreatheSessionEntity>> = repository.getAllSessions()
    
    fun getTotalDuration(): Flow<Int> = repository.getTotalBreatheDurationFlow().map { it ?: 0 }
}
