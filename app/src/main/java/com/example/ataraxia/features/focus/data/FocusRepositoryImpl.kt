package com.example.ataraxia.features.focus.data

import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.entity.FocusSessionEntity
import com.example.ataraxia.data.local.entity.FocusIntentionEntity
import com.example.ataraxia.features.focus.domain.FocusRepository
import kotlinx.coroutines.flow.Flow

class FocusRepositoryImpl(
    private val focusDao: FocusDao
) : FocusRepository {
    override fun getAllSessions(): Flow<List<FocusSessionEntity>> = focusDao.getAllSessions()
    override fun getTotalFocusDurationFlow(): Flow<Int?> = focusDao.getTotalFocusDurationFlow()
    override suspend fun insertSession(session: FocusSessionEntity) {
        focusDao.insertSession(session)
    }
    override suspend fun deleteSession(id: Long) {
        focusDao.deleteSession(id)
    }
    override suspend fun clearAllSessions() {
        focusDao.clearAllSessions()
    }

    // Custom intentions implementation
    override fun getAllCustomIntentions(): Flow<List<FocusIntentionEntity>> = focusDao.getAllCustomIntentions()
    override suspend fun insertIntention(intention: FocusIntentionEntity) {
        focusDao.insertIntention(intention)
    }
    override suspend fun deleteIntention(name: String) {
        focusDao.deleteIntention(name)
    }
}
