package com.example.ataraxia.features.focus.domain

import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import kotlinx.coroutines.flow.Flow

interface FocusRepository {
    // Focus Session Logs
    fun getAllSessions(): Flow<List<FocusSessionEntity>>
    fun getTotalFocusDurationFlow(): Flow<Int?>
    suspend fun insertSession(session: FocusSessionEntity)
    suspend fun deleteSession(id: Long)
    suspend fun clearAllSessions()

    // Focus Intentions
    fun getAllCustomIntentions(): Flow<List<FocusIntentionEntity>>
    suspend fun insertIntention(intention: FocusIntentionEntity)
    suspend fun deleteIntention(name: String)
}
