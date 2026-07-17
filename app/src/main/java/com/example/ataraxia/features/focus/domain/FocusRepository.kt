package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    fun getAllSessions(): Flow<List<FocusSessionEntity>>
    fun getTotalFocusDurationFlow(): Flow<Int?>
    suspend fun insertSession(session: FocusSessionEntity)
    suspend fun deleteSession(id: Long)
    suspend fun clearAllSessions()
}
