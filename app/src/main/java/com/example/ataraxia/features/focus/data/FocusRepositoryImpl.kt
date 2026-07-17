package com.example.ataraxia.features.focus.data

import com.example.ataraxia.data.local.dao.FocusDao
import com.example.ataraxia.data.local.entity.FocusSessionEntity
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
}
