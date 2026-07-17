package com.example.ataraxia.features.breathe.domain

import com.example.ataraxia.data.local.entity.BreatheSessionEntity
import kotlinx.coroutines.flow.Flow

interface BreatheRepository {
    fun getAllSessions(): Flow<List<BreatheSessionEntity>>
    fun getTotalBreatheDurationFlow(): Flow<Int?>
    suspend fun insertSession(session: BreatheSessionEntity)
    suspend fun deleteSession(id: Long)
    suspend fun clearAllSessions()
}
